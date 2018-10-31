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

	protected void setup()
	{
		Object args[] = getArguments();
		register(args[0].toString(), args[1].toString());
		price = new Tariff(Integer.parseInt(args[2].toString()));
		setBuyingPrice = Double.parseDouble(args[3].toString());
		acceptedBuyPriceThreshold = Double.parseDouble(args[4].toString());
		acceptedSellPriceThreshold = Double.parseDouble(args[5].toString());
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

		private IteratedR(Agent agent, MessageTemplate template)
		{
			super(agent, template);
		}

		protected Behaviour createResponder(ACLMessage message)
		{
			System.out.println("createResponder for " + myAgent.getLocalName());
			return new SSIteratedContractNetResponder(myAgent, message)
			{
				protected ACLMessage handleCfp(ACLMessage cfp)
						throws RefuseException, FailureException, NotUnderstoodException
				{
					System.out.println("Agent " + cfp.getSender().getName() + " counter-proposed " + cfp.getContent().split(",")[1]); // always gets the original
																						// CFP!!!
					String vals[] = cfp.getContent().split(",");
					if (vals[0].equals("buy") || vals[0].equals("counter-buy"))
					{
						try
						{
							if(currentOffer == 0)currentOffer = price.getPrice(Double.parseDouble(vals[1]));
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
						currentOffer *= 0.95;
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
						currentOffer *= 1.05;
					}

					return proposal;
				}

				protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
						throws FailureException
				{
					currentOffer = 0;
					System.out.println("Agent " + getLocalName() + ": Action successfully performed");
					ACLMessage inform = accept.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					return inform;
				}

				protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject)
				{
					
				}
			};
		}
	}
}
