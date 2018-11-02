package set;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.awt.EventQueue;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.jfree.ui.RefineryUtilities;

@SuppressWarnings("serial")
public class HomeAgent extends Agent
{
	private SequentialBehaviour seq;
	private ParallelBehaviour par;
	private boolean trade = false;
	private double predictedDemand = 0;
	private double predictedGeneration = 0;
	private double actualDemand = 0;
	private double actualGeneration = 0;
	private AID[] raAgents;
	private AID[] aaGAgents;
	private AID[] aaCAgents;
	private double acceptedBuyPriceThreshold;
	private double acceptedSellPriceThreshold;
	private int nAAGResponders;
	private int nAACResponders;
	private int nRAResponders;
	private double setBuyingPrice;
	private double setSellingPrice;
	private double originalBuyingPrice;
	private double originalSellingPrice;
	private String tou;
	transient protected WOEPlot myGui;
	
	NegotiationConduction selectedConduction;
	
	enum NegotiationConduction  {
		SINGLE_ROUND,
		MULTI_ROUND
	}
	
	protected void setup()
	{
		Object[] args = getArguments();
		myGui = new WOEPlot("GUI");
		myGui.pack();
		RefineryUtilities.centerFrameOnScreen(myGui);
		myGui.setVisible(true);
		myGui.start();
		myGui.pushData(args[0].toString(), args[1].toString(), args[2].toString(), args[3].toString());

		if (args != null && args.length > 0)
		{
			acceptedBuyPriceThreshold = Double.parseDouble(args[0].toString());
			setBuyingPrice = Double.parseDouble(args[1].toString());
			acceptedSellPriceThreshold = Double.parseDouble(args[2].toString());
			setSellingPrice = Double.parseDouble(args[3].toString());
			originalSellingPrice = setSellingPrice;
			originalBuyingPrice = setBuyingPrice;
			trade = false;
			setBuyingPrice = originalBuyingPrice;
			setSellingPrice = originalSellingPrice;
			predictedDemand = 0;
			predictedGeneration = 0;
			myGui.updateLog("Acceptable price threshold set to: " + acceptedBuyPriceThreshold);
			addBehaviour(new TickerBehaviour(this, 5000)
			{
				protected void onTick()
				{
					// register the services
					acceptedBuyPriceThreshold = Double.parseDouble(myGui.getMaxBuy());
					setBuyingPrice = Double.parseDouble(myGui.getMinBuy());
					acceptedSellPriceThreshold = Double.parseDouble(myGui.getMinSell());
					setSellingPrice = Double.parseDouble(myGui.getMaxBuy());
					myGui.updateLog("Max Buy price set:" + acceptedBuyPriceThreshold);
					myGui.updateLog("Min Buy price set:" + setBuyingPrice);
					myGui.updateLog("Min Sell price set:" + acceptedSellPriceThreshold);
					myGui.updateLog("Max Sell price set:" + setSellingPrice);
					predictedDemand = 0;
					predictedGeneration = 0;
					actualDemand = 0;
					actualGeneration = 0;
					trade = false;
					seq = new SequentialBehaviour();
					
					if (args[4].equals("single")) {
						selectedConduction = NegotiationConduction.SINGLE_ROUND;
					} else if (args[4].equals("multi")) {
						selectedConduction = NegotiationConduction.MULTI_ROUND;
					} else {
						System.out.println("Negotiation conduction not recognized for Agent " + getLocalName());
						System.out.println("Defaulting to multi-round negotiation.");			
						selectedConduction = NegotiationConduction.MULTI_ROUND;
					}

					par = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
					// Phase 1: Send request for energy demand to Appliances with consumption energy
					// need
					aaCAgents = getService("AAConsumer");
					nAACResponders = aaCAgents.length;
					ACLMessage aaCMsg = createMessage(nAACResponders, aaCAgents, "Predicted demand?",
							FIPANames.InteractionProtocol.FIPA_REQUEST, ACLMessage.REQUEST);

					aaGAgents = getService("AAGenerator");
					nAAGResponders = aaGAgents.length;
					ACLMessage aaGMsg = createMessage(nAAGResponders, aaGAgents, "Predicted generation?",
							FIPANames.InteractionProtocol.FIPA_REQUEST, ACLMessage.REQUEST);

					raAgents = getService("RA");
					nRAResponders = raAgents.length;

					par.addSubBehaviour(new AchieveREInitiator(myAgent, aaCMsg)
					{

						protected void handleAgree(ACLMessage agree)
						{
						}

						protected void handleInform(ACLMessage inform)
						{
							myGui.updateLog(getLocalName() + ": " + inform.getSender().getLocalName()
									+ " successfully performed the requested action");
							myGui.updateLog(getLocalName() + ": " + inform.getSender().getLocalName()
									+ "'s predicted energy demand is " + inform.getContent());
							String vals[] = inform.getContent().split(",");
							Double a = Double.parseDouble(vals[0]);
							Double b = Double.parseDouble(vals[1]);
							actualDemand += a;
							predictedDemand += b;
							tou = vals[2];
						}

						protected void handleRefuse(ACLMessage refuse)
						{
							nAAGResponders--;
						}

						protected void handleFailure(ACLMessage failure)
						{

						}

						protected void handleAllResultNotifications(Vector notifications)
						{
							if (notifications.size() < nAAGResponders)
							{
								myGui.updateLog(getLocalName() + ": " + "Timeout expired: missing "
										+ (nAACResponders - notifications.size()) + " responses");
							} else
							{
								System.out
										.println(getLocalName() + ": " + "Received notifications from every responder");
								myGui.updateLog("Predicted Demand is:" + predictedDemand);
								myGui.dataUpdate(actualDemand, predictedDemand);
							}
						}
					});

					par.addSubBehaviour(new AchieveREInitiator(myAgent, aaGMsg)
					{

						protected void handleAgree(ACLMessage agree)
						{
							myGui.updateLog(
									getLocalName() + ": " + agree.getSender().getName() + " has agreed to the request");
						}

						protected void handleInform(ACLMessage inform)
						{
							String vals[] = inform.getContent().split(",");
							Double a = Double.parseDouble(vals[0]);
							Double b = Double.parseDouble(vals[1]);
							actualGeneration += a;
							predictedGeneration += b;
							tou = vals[2];
						}

						protected void handleRefuse(ACLMessage refuse)
						{
							myGui.updateLog(getLocalName() + ": " + refuse.getSender().getLocalName()
									+ " refused to perform the requested action");
							nAAGResponders--;
						}

						protected void handleFailure(ACLMessage failure)
						{
							if (failure.getSender().equals(myAgent.getAMS()))
							{
								myGui.updateLog(getLocalName() + ": " + "Responder does not exist");
							} else
							{
								myGui.updateLog(getLocalName() + ": " + failure.getSender().getLocalName()
										+ " failed to perform the requested action.");
							}
						}

						protected void handleAllResultNotifications(Vector notifications)
						{
							if (notifications.size() < nAAGResponders)
							{
								myGui.updateLog(getLocalName() + ": " + "Timeout expired: missing "
										+ (nAAGResponders - notifications.size()) + " responses");
							} else
							{
								System.out
										.println(getLocalName() + ": " + "Received notifications from every responder");
								myGui.updateLog("Predicted Generation is:" + predictedGeneration);
							}
						}
					});

					seq.addSubBehaviour(par);
					
					if (selectedConduction.equals(NegotiationConduction.SINGLE_ROUND)) {
						seq.addSubBehaviour(singleRoundNegotiator);
					} else {
						seq.addSubBehaviour(negotiator);
					}
					
					addBehaviour(seq);
				}
			});
		}
	}

	private ACLMessage createMessage(int receivers, AID[] agents, String content, String protocol, int type)
	{
		ACLMessage result = new ACLMessage(type);
		for (int i = 0; i < receivers; ++i)
		{
			result.addReceiver(agents[i]);
		}
		result.setProtocol(protocol);
		result.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		result.setContent(content);
		return result;
	}

	private AID[] getService(String service)
	{
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(service);
		dfd.addServices(sd);
		try
		{
			DFAgentDescription[] result = DFService.search(this, dfd);
			AID[] agents = new AID[result.length];
			for (int i = 0; i < result.length; ++i)
			{
				agents[i] = result[i].getName();
			}
			return agents;

		} catch (FIPAException fe)
		{
			fe.printStackTrace();
		}
		return null;
	}

	OneShotBehaviour negotiator = new OneShotBehaviour()
	{

		@Override
		public void action()
		{
			String msgContent;
			if (predictedDemand > predictedGeneration)
			{
				msgContent = "buy," + Double.toString(predictedDemand - predictedGeneration)+","+tou;
				ACLMessage raMsg = createMessage(nRAResponders, raAgents, msgContent,
						FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, ACLMessage.CFP);
				myGui.updateLog("Agent " + getLocalName() + " requesting to " + nRAResponders + " agents to " + msgContent + " wH electricity." );
				seq.addSubBehaviour(new Iterated(myAgent, raMsg));
			} else
			{
				msgContent = "sell," + Double.toString(predictedGeneration - predictedDemand)+","+tou;
				ACLMessage raMsg = createMessage(nRAResponders, raAgents, msgContent,
						FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, ACLMessage.CFP);
				myGui.updateLog("Agent " + getLocalName() + " requesting to " + nRAResponders + " agents to " + msgContent + " wH electricity." );
				seq.addSubBehaviour(new IteratedS(myAgent, raMsg));
			}

		}

	};

	OneShotBehaviour singleRoundNegotiator = new OneShotBehaviour()
	{

		@Override
		public void action()
		{
			String msgContent;
			if (predictedDemand > predictedGeneration)
			{
				msgContent = "buy," + Double.toString(predictedDemand - predictedGeneration)+","+tou;
				ACLMessage raMsg = createMessage(nRAResponders, raAgents, msgContent,
						FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, ACLMessage.CFP);
				// NEW
				seq.addSubBehaviour(new SingleNegotiator(myAgent, raMsg));
				// END NEW
			} else
			{
				msgContent = "sell," + Double.toString(predictedGeneration - predictedDemand)+","+tou;
				ACLMessage raMsg = createMessage(nRAResponders, raAgents, msgContent,
						FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, ACLMessage.CFP);
				// NEW
				seq.addSubBehaviour(new SingleNegotiator(myAgent, raMsg));
				// END NEW
			}

		}

	};
	
	private class SingleNegotiator extends Iterated
	{
		public SingleNegotiator(Agent a, ACLMessage cfp) {
			super(a, cfp);
		}

		@Override
		protected void handleAllResponses(Vector responses, Vector acceptances)
		{

			if (responses.size() < nRAResponders)
			{
				// Some responder didn't reply within the specified timeout
				System.out.println("Timeout expired: missing " + (nRAResponders - responses.size()) + " responses");
			}
			// Evaluate proposals.
			double bestProposal = 9999999;
			AID bestProposer = null;
			ACLMessage accept = null;
			Enumeration e = responses.elements();
			while (e.hasMoreElements())
			{
				ACLMessage msg = (ACLMessage) e.nextElement();
				if (msg.getPerformative() == ACLMessage.PROPOSE)
				{
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					double proposal = Double.parseDouble(msg.getContent());
					if (proposal < bestProposal)
					{
						bestProposal = proposal;
						bestProposer = msg.getSender();
						accept = reply;
						trade = true;
					} 
					acceptances.addElement(reply);
				}
			}
			// Accept the proposal of the best proposer
			if (accept != null)
			{
				System.out.println(
						"Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			}
			getDataStore().put(ALL_CFPS_KEY, acceptances);
		}
	}
	
	private class Iterated extends ContractNetInitiator
	{

		public Iterated(Agent a, ACLMessage cfp)
		{
			super(a, cfp);
		}

		protected void handlePropose(ACLMessage propose, Vector v)
		{
			myGui.updateLog("Agent " + propose.getSender().getLocalName() + " proposed " + propose.getContent());
		}

		protected void handleRefuse(ACLMessage refuse)
		{
			myGui.updateLog("Agent " + refuse.getSender().getLocalName() + " refused.");
		}

		protected void handleFailure(ACLMessage failure)
		{
			if (failure.getSender().equals(myAgent.getAMS()))
			{
				// FAILURE notification from the JADE runtime: the receiver
				// does not exist
				myGui.updateLog("Responder does not exist");
			} else
			{

				myGui.updateLog("Negotiation with Agent " + failure.getSender().getLocalName() + " ended.");
			}
			// Immediate failure --> we will not receive a response from this agent
			nRAResponders--;
		}

		protected void handleAllResponses(Vector responses, Vector acceptances)
		{

			if (setBuyingPrice <= acceptedBuyPriceThreshold && !trade)
			{
				reset();
				setBuyingPrice *= 1.05;
				if (responses.size() < nRAResponders)
				{
					// Some responder didn't reply within the specified timeout
					myGui.updateLog("Timeout expired: missing " + (nRAResponders - responses.size()) + " responses");
				}
				// Evaluate proposals.
				double bestProposal = 9999999;
				AID bestProposer = null;
				ACLMessage accept = null;
				Enumeration e = responses.elements();
				while (e.hasMoreElements())
				{
					ACLMessage msg = (ACLMessage) e.nextElement();
					if (msg.getPerformative() == ACLMessage.PROPOSE)
					{
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						double proposal = Double.parseDouble(msg.getContent());
						if (proposal <= setBuyingPrice && proposal < bestProposal)
						{
							bestProposal = proposal;
							bestProposer = msg.getSender();
							accept = reply;
							trade = true;
						} 
						if(accept==null)
						{
							reply.setPerformative(ACLMessage.CFP);
							reply.setContent("counter-buy," + (Double.toString(setBuyingPrice)) + ","+ tou);
							myGui.updateLog("Agent " + getLocalName() + " counter proposing " + (Double.toString(setBuyingPrice)) + "." );
						}
						acceptances.addElement(reply);
					}
				}
				// Accept the proposal of the best proposer
				if (accept != null)
				{
					myGui.updateLog(
							"Accepting proposal " + bestProposal + " from responder " + bestProposer.getLocalName());
					accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				}
				getDataStore().put(ALL_CFPS_KEY, acceptances);
			}
			else
			{
				Enumeration elements = responses.elements();
				while (elements.hasMoreElements())
				{
					ACLMessage msg = (ACLMessage) elements.nextElement();
					if (msg.getPerformative() == ACLMessage.PROPOSE)
					{
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						acceptances.addElement(reply);
					}
				}
			}
		}

		protected void handleInform(ACLMessage inform)
		{
			System.out
					.println("Agent " + inform.getSender().getLocalName() + " successfully performed the requested action");
		}
	}
	
	private class IteratedS extends ContractNetInitiator
	{

		public IteratedS(Agent a, ACLMessage cfp)
		{
			super(a, cfp);
		}

		protected void handlePropose(ACLMessage propose, Vector v)
		{
			myGui.updateLog("Agent " + propose.getSender().getLocalName() + " proposed " + propose.getContent());
		}

		protected void handleRefuse(ACLMessage refuse)
		{
			myGui.updateLog("Agent " + refuse.getSender().getLocalName() + " refused.");
		}

		protected void handleFailure(ACLMessage failure)
		{
			if (failure.getSender().equals(myAgent.getAMS()))
			{
				// FAILURE notification from the JADE runtime: the receiver
				// does not exist
				myGui.updateLog("Responder does not exist");
			} else
			{

				myGui.updateLog("Negotiation with Agent " + failure.getSender().getLocalName() + " ended.");
			}
			// Immediate failure --> we will not receive a response from this agent
			nRAResponders--;
		}

		protected void handleAllResponses(Vector responses, Vector acceptances)
		{

			if (setSellingPrice >= acceptedSellPriceThreshold && !trade)
			{
				reset();
				setSellingPrice *= 0.95;
				if (responses.size() < nRAResponders)
				{
					// Some responder didn't reply within the specified timeout
					myGui.updateLog("Timeout expired: missing " + (nRAResponders - responses.size()) + " responses");
				}
				// Evaluate proposals.
				double bestProposal = 0;
				AID bestProposer = null;
				ACLMessage accept = null;
				Enumeration e = responses.elements();
				while (e.hasMoreElements())
				{
					ACLMessage msg = (ACLMessage) e.nextElement();
					if (msg.getPerformative() == ACLMessage.PROPOSE)
					{
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						double proposal = Double.parseDouble(msg.getContent());
						if (proposal >= setSellingPrice && proposal > bestProposal)
						{
							bestProposal = proposal;
							bestProposer = msg.getSender();
							accept = reply;
							trade = true;
						} 
						if(accept==null)
						{
							reply.setPerformative(ACLMessage.CFP);
							reply.setContent("counter-sell," + (Double.toString(setSellingPrice)) + ","+ tou);
							myGui.updateLog("Agent " + getLocalName() + " counter proposing " + (Double.toString(setSellingPrice)) + "." );
						}
						acceptances.addElement(reply);
					}
				}
				// Accept the proposal of the best proposer
				if (accept != null)
				{
					myGui.updateLog(
							"Accepting proposal " + bestProposal + " from responder " + bestProposer.getLocalName());
					accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				}
				getDataStore().put(ALL_CFPS_KEY, acceptances);
			}
			else
			{
				Enumeration elements = responses.elements();
				while (elements.hasMoreElements())
				{
					ACLMessage msg = (ACLMessage) elements.nextElement();
					if (msg.getPerformative() == ACLMessage.PROPOSE)
					{
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						acceptances.addElement(reply);
					}
				}
			}
		}

		protected void handleInform(ACLMessage inform)
		{
			myGui.updateLog("Agent " + inform.getSender().getName() + " successfully performed the requested action");
		}
	}
}
