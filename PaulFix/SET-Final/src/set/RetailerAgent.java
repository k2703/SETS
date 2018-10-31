package set;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
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

		addBehaviour(new ContractNetResponder(this, template)
		{
			protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException
			{
				System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName()
						+ ". Action is " + cfp.getContent());
				String vals[] = cfp.getContent().split(",");
				if (vals[0] == "buy")
				{
					try
					{
						currentOffer = price.getPrice(Double.parseDouble(vals[1]));
					} catch (NumberFormatException e)
					{
						e.printStackTrace();
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					act = "selling";
					// We provide a proposal
					System.out.println("Agent " + getLocalName() + ": Proposing " + Double.toString(currentOffer));
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent(String.valueOf(currentOffer));
					return propose;
				} else
				{
					currentOffer = setBuyingPrice;
					act = "buying";
					System.out.println("Agent " + getLocalName() + ": Proposing " + Double.toString(currentOffer));
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent(String.valueOf(currentOffer));
					return propose;
				}
			}

			protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
					throws FailureException
			{
				System.out.println("Agent " + getLocalName() + ": Proposal accepted");
				if(act == "selling")
				{
					System.out.println("Agent " + getLocalName() + ": Action successfully performed");
					ACLMessage inform = accept.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					return inform;
				}
				else
				{
					System.out.println("Agent " + getLocalName() + ": Action execution failed");
					throw new FailureException("unexpected-error");
				}
			}

			protected ACLMessage handleRejectProposal(ACLMessage reject)
			{
				System.out.println("Agent " + getLocalName() + ": Proposal rejected");
				ACLMessage propose = reject.createReply();
				propose.setPerformative(ACLMessage.PROPOSE);
				String content;
				if(act == "selling")
				{
					currentOffer*= 0.95;
					if(currentOffer >= acceptedSellPriceThreshold)
					propose.setContent(Double.toString(currentOffer));
					else propose.setPerformative(ACLMessage.FAILURE);
				}
				else
				{
					currentOffer*= 1.05;
					if(currentOffer <= acceptedBuyPriceThreshold)
					propose.setContent(Double.toString(currentOffer));
					else propose.setPerformative(ACLMessage.FAILURE);
				}
				return propose;
			}
		});
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
}
