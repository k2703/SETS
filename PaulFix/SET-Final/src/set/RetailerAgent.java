package set;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.proto.SSIteratedContractNetResponder;
import jade.proto.SSResponderDispatcher;
import jade.proto.ContractNetInitiator;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;

public class RetailerAgent extends Agent
{
	private Tariff price;
	private double currentOffer;
	private double setBuyingPrice;
	private double acceptedBuyPriceThreshold;
	private double acceptedSellPriceThreshold;
	private String act;
	private int round;
	private String pricetype;
	String nego;
	
	public String[] getAgentParas()
	{
		String result[] = new String[5];
		result[0] = Double.toString(setBuyingPrice);
		result[1] = Double.toString(acceptedBuyPriceThreshold);
		result[2] = Double.toString(acceptedSellPriceThreshold);
		switch(pricetype)
		{
			case "1":
				result[3] = "Fixed";
			case "2":
				result[3] = "Block Rate";
			case "3":
				result[3] = "Time Of Use";
		}
		result[4] = nego;
		return result;
	}
	
	NegotiationStrategy selectedStrategy;
	
	enum NegotiationStrategy  {
		PERCENT_5,
		CREMENT_2,
		WEIGHTED_AVG
	}
	

	protected void setup()
	{
		Object args[] = getArguments();
		register(args[0].toString(), args[1].toString());
		pricetype = args[1].toString();
		price = new Tariff(Integer.parseInt(args[2].toString()));
		setBuyingPrice = Double.parseDouble(args[3].toString());
		acceptedBuyPriceThreshold = Double.parseDouble(args[4].toString());
		acceptedSellPriceThreshold = Double.parseDouble(args[5].toString());
		nego = args[6].toString();
		if (args[6].equals("percent5")) {
				selectedStrategy = NegotiationStrategy.PERCENT_5;
		} else if (args[6].equals("crement2")) {
				selectedStrategy = NegotiationStrategy.CREMENT_2;
		} else if (args[6].equals("weightedavg")) {
				selectedStrategy = NegotiationStrategy.WEIGHTED_AVG;
		} else {
			// if negotiation strategy is unrecognized, default to percent5
			System.out.println("Negotiation strategy not recognized for Agent " + getLocalName());
			System.out.println("Defaulting to Increase/Decrease by 5%");
			selectedStrategy = NegotiationStrategy.PERCENT_5;
		}
		System.out.println("Agent " + getLocalName() + " waiting for CFP...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		addBehaviour(new IteratedR(this, template));
	}

	private void register(String name, String type)
	{
		ServiceDescription sd = new ServiceDescription();
		sd.setName(name);
		sd.setType(type);
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		dfd.addServices(sd);
		try
		{
			DFService.register(this, dfd);
		} catch (FIPAException fe)
		{
			fe.printStackTrace();
		}
	}

	protected void takeDown()
	{
		try
		{

			DFService.deregister(this);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private class IteratedR extends SSResponderDispatcher
	{
		private double generateSellOffer(double aOurOffer, double aReceivedOffer, int round) {
			double result = aOurOffer;
			
			if (selectedStrategy.equals(NegotiationStrategy.PERCENT_5)) {
				// decrease our sell offer by 5%
				result = aOurOffer * 0.95;
			} else if (selectedStrategy.equals(NegotiationStrategy.CREMENT_2)) {
				// decrement our buy offer by 2
				result = aOurOffer - 2;
			} else if (selectedStrategy.equals(NegotiationStrategy.WEIGHTED_AVG)) {
				// the first message received is the energy usage, not the offer
				// we shouldn't use it to calculate a price
				if (round > 1) {
					// this occurs on a one-round delay
					result = (( aOurOffer * 2) + aReceivedOffer ) / 3;
				}
				
			}
			
			// return the new sell price
			return result;
		}
		
		private double generateBuyOffer(double aOurOffer, double aReceivedOffer, int round) {
			double result = aOurOffer;
			
			if (selectedStrategy.equals(NegotiationStrategy.PERCENT_5)) {
				// increase our buy offer by 5%
				result = aOurOffer * 1.05;
			} else if (selectedStrategy.equals(NegotiationStrategy.CREMENT_2)) {
				// increment our buy offer by 2
				result = aOurOffer + 2;
			} else if (selectedStrategy.equals(NegotiationStrategy.WEIGHTED_AVG)) {
				// the first message received is the energy usage, not the offer
				// we shouldn't use it to calculate a price
				if (round > 1) {
					// this occurs on a one-round delay
					result = (( aOurOffer * 2) + aReceivedOffer ) / 3;
				}	
			}
			
			// return the new buy price
			return result;
		}

		private IteratedR(Agent agent, MessageTemplate template)
		{
			super(agent, template);
		}

		protected Behaviour createResponder(ACLMessage message)
		{
			round = 1;
			System.out.println("createResponder for " + myAgent.getLocalName());
			return new SSIteratedContractNetResponder(myAgent, message)
			{
				protected ACLMessage handleCfp(ACLMessage cfp)
						throws RefuseException, FailureException, NotUnderstoodException
				{
					if(!(cfp.getContent().split(",")[0].equals("buy") || cfp.getContent().split(",")[0].equals("sell")))
					System.out.println("Agent " + cfp.getSender().getName() + " counter-proposed " + cfp.getContent().split(",")[1]); // always gets the original
					else
						System.out.println("Agent " + cfp.getSender().getName() + " proposed to " + cfp.getContent()); // always gets the original
					String vals[] = cfp.getContent().split(",");
					if (vals[0].equals("buy") || vals[0].equals("counter-buy"))
					{
						try
						{
							if(currentOffer == 0)currentOffer = price.getPrice(Double.parseDouble(vals[1]), vals[2]);
						} catch (NumberFormatException e)
						{
							e.printStackTrace();
						} catch (Exception e)
						{
							e.printStackTrace();
						}
						act = "selling";
					} else
					{
						currentOffer = setBuyingPrice;
						act = "buying";
					}
					ACLMessage proposal = cfp.createReply();
					proposal.setPerformative(ACLMessage.PROPOSE);
					String content;
					if (act == "selling")
					{
						if (currentOffer >= acceptedSellPriceThreshold)
						{
							proposal.setContent(Double.toString(currentOffer));
						}
						else
						{
							proposal.setPerformative(ACLMessage.FAILURE);
						}
						// generate the new sell price
						currentOffer = generateSellOffer(currentOffer, Double.parseDouble(vals[1]), round);
						// currentOffer *= 0.95;
					} 
					else
					{
						if (currentOffer <= acceptedBuyPriceThreshold)
						{
							proposal.setContent(Double.toString(currentOffer));
						}
						else
						{
							proposal.setPerformative(ACLMessage.FAILURE);
						}
						// generate the new buy price
						currentOffer = generateBuyOffer(currentOffer, Double.parseDouble(vals[1]), round);
						// currentOffer *= 1.05;
					}

					// it is the next round
					round ++;
					return proposal;
				}

				protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
						throws FailureException
				{
					currentOffer = 0;
					System.out.println("Agent " + getLocalName() + ": Action successfully performed");
					ACLMessage inform = accept.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					// reset the round counter
					round = 1;
					return inform;
				}

				protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject)
				{
					
				}
			};
		}
	}
}
