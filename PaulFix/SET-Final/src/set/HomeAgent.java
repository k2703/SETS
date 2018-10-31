package set;

import jade.core.Agent;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

@SuppressWarnings("serial")
public class HomeAgent extends Agent
{
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

	protected void setup()
	{
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
			acceptedBuyPriceThreshold = (double) args[0];
			setBuyingPrice = (double) args[1];
			acceptedSellPriceThreshold = (double) args[2];
			setSellingPrice = (double) args[3];
			originalSellingPrice = setSellingPrice;
			originalBuyingPrice = setBuyingPrice;
			
			System.out.println("Acceptable price threshold set to: " + acceptedBuyPriceThreshold);
			addBehaviour(new TickerBehaviour(this, 60000)
			{
				protected void onTick()
				{
					// register the services

					SequentialBehaviour seq = new SequentialBehaviour();
					addBehaviour(seq);

					ParallelBehaviour par = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
					// Phase 1: Send request for energy demand to Appliances with consumption energy
					// need
					aaCAgents = getService("AAConsumer");
					nAACResponders = aaCAgents.length;
					ACLMessage aaCMsg = createMessage(	nAACResponders, 
														aaCAgents, 
														"Predicted demand?", 
														FIPANames.InteractionProtocol.FIPA_REQUEST, 
														ACLMessage.REQUEST);
					
					aaGAgents = getService("AAGenerator");
					nAAGResponders = aaGAgents.length;
					ACLMessage aaGMsg = createMessage(	nAAGResponders, 
														aaGAgents, 
														"Predicted generation?", 
														FIPANames.InteractionProtocol.FIPA_REQUEST, 
														ACLMessage.REQUEST);

					raAgents = getService("RA");
					nRAResponders = raAgents.length;

					par.addSubBehaviour(new AchieveREInitiator(myAgent, aaCMsg)
					{

						protected void handleAgree(ACLMessage agree)
						{
							System.out.println(
									getLocalName() + ": " + agree.getSender().getName() + " has agreed to the request");
						}

						protected void handleInform(ACLMessage inform)
						{
							System.out.println(getLocalName() + ": " + inform.getSender().getName()
									+ " successfully performed the requested action");
							System.out.println(getLocalName() + ": " + inform.getSender().getName()
									+ "'s predicted energy demand is " + inform.getContent());
							String vals[] = inform.getContent().split(",");
							actualDemand += Double.parseDouble(vals[0]);
							predictedDemand += Double.parseDouble(vals[1]);
						}

						protected void handleRefuse(ACLMessage refuse)
						{
							System.out.println(getLocalName() + ": " + refuse.getSender().getName()
									+ " refused to perform the requested action.");
							nAAGResponders--;
						}

						protected void handleFailure(ACLMessage failure)
						{
							if (failure.getSender().equals(myAgent.getAMS()))
							{
								System.out.println(getLocalName() + ": " + "Responder does not exist");
							} else
							{
								System.out.println(getLocalName() + ": " + failure.getSender().getName()
										+ " failed to perform the requested action.");
							}
						}

						protected void handleAllResultNotifications(Vector notifications)
						{
							if (notifications.size() < nAAGResponders)
							{
								System.out.println(getLocalName() + ": " + "Timeout expired: missing "
										+ (nAACResponders - notifications.size()) + " responses");
							} else
							{
								System.out
										.println(getLocalName() + ": " + "Received notifications from every responder");
							}
						}
					});

					par.addSubBehaviour(new AchieveREInitiator(myAgent, aaGMsg)
					{

						protected void handleAgree(ACLMessage agree)
						{
							System.out.println(
									getLocalName() + ": " + agree.getSender().getName() + " has agreed to the request");
						}

						protected void handleInform(ACLMessage inform)
						{
							System.out.println(getLocalName() + ": " + inform.getSender().getName()
									+ " successfully performed the requested action");
							System.out.println(getLocalName() + ": " + inform.getSender().getName()
									+ "'s predicted energy generation is " + inform.getContent());
							String vals[] = inform.getContent().split(",");
							actualGeneration += Double.parseDouble(vals[0]);
							predictedGeneration += Double.parseDouble(vals[1]);
						}

						protected void handleRefuse(ACLMessage refuse)
						{
							System.out.println(getLocalName() + ": " + refuse.getSender().getName()
									+ " refused to perform the requested action");
							nAAGResponders--;
						}

						protected void handleFailure(ACLMessage failure)
						{
							if (failure.getSender().equals(myAgent.getAMS()))
							{
								System.out.println(getLocalName() + ": " + "Responder does not exist");
							} else
							{
								System.out.println(getLocalName() + ": " + failure.getSender().getName()
										+ " failed to perform the requested action.");
							}
						}

						protected void handleAllResultNotifications(Vector notifications)
						{
							if (notifications.size() < nAAGResponders)
							{
								System.out.println(getLocalName() + ": " + "Timeout expired: missing "
										+ (nAAGResponders - notifications.size()) + " responses");
							} else
							{
								System.out
										.println(getLocalName() + ": " + "Received notifications from every responder");
							}
						}
					});
					
					seq.addSubBehaviour(par);

					String msgContent;
					if(predictedDemand > predictedGeneration)
					{
						msgContent = "buy," + Double.toString(predictedDemand-predictedGeneration);
						ACLMessage raMsg = createMessage(	nRAResponders, 
								raAgents, 
								msgContent, 
								FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, 
								ACLMessage.CFP);
						seq.addSubBehaviour(new ContractNetInitiator(myAgent, raMsg)
						{

							protected void handlePropose(ACLMessage propose, Vector v)
							{
								System.out.println(
										"Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
							}

							protected void handleRefuse(ACLMessage refuse)
							{
								System.out.println("Agent " + refuse.getSender().getName() + " refused.");
								setBuyingPrice = originalBuyingPrice;
							}

							protected void handleFailure(ACLMessage failure)
							{
								if (failure.getSender().equals(myAgent.getAMS()))
								{
									// FAILURE notification from the JADE runtime: the receiver
									// does not exist
									System.out.println("Responder does not exist");
								} else
								{
									setBuyingPrice = originalBuyingPrice;
									
									System.out.println("Negotiation with Agent " + failure.getSender().getName() + " ended.");
								}
								// Immediate failure --> we will not receive a response from this agent
								nRAResponders--;
							}

							protected void handleAllResponses(Vector responses, Vector acceptances)
							{
								setBuyingPrice*=1.05;
								if (responses.size() < nRAResponders)
								{
									// Some responder didn't reply within the specified timeout
									System.out.println(
											"Timeout expired: missing " + (nRAResponders - responses.size()) + " responses");
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
										}
										else
										{
											if(setBuyingPrice > acceptedBuyPriceThreshold)
											{
												reply.setPerformative(ACLMessage.FAILURE);
											}
											else
											{
												reply.setContent(Double.toString(setBuyingPrice));
											}
											
										}
										acceptances.addElement(reply);
									}
								}
								// Accept the proposal of the best proposer
								if (accept != null)
								{
									System.out.println("Accepting proposal " + bestProposal + " from responder "
											+ bestProposer.getName());
									accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
								}
							}

							protected void handleInform(ACLMessage inform)
							{
								System.out.println("Agent " + inform.getSender().getName()
										+ " successfully performed the requested action");
								setBuyingPrice = originalBuyingPrice;
							}
						});
					}
					else 
					{
						msgContent = "sell," + Double.toString(predictedGeneration-predictedDemand);
						ACLMessage raMsg = createMessage(	nRAResponders, 
								raAgents, 
								msgContent, 
								FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, 
								ACLMessage.CFP);
						seq.addSubBehaviour(new ContractNetInitiator(myAgent, raMsg)
						{

							protected void handlePropose(ACLMessage propose, Vector v)
							{
								System.out.println(
										"Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
							}

							protected void handleRefuse(ACLMessage refuse)
							{
								System.out.println("Agent " + refuse.getSender().getName() + " refused.");
								setSellingPrice = originalSellingPrice;
							}

							protected void handleFailure(ACLMessage failure)
							{
								if (failure.getSender().equals(myAgent.getAMS()))
								{
									// FAILURE notification from the JADE runtime: the receiver
									// does not exist
									System.out.println("Responder does not exist");
								} else
								{
									setSellingPrice = originalSellingPrice;
									System.out.println("Negotiation with Agent " + failure.getSender().getName() + " ended.");
								}
								// Immediate failure --> we will not receive a response from this agent
								nRAResponders--;
							}

							protected void handleAllResponses(Vector responses, Vector acceptances)
							{
								setSellingPrice*=0.95;
								if (responses.size() < nRAResponders)
								{
									// Some responder didn't reply within the specified timeout
									System.out.println(
											"Timeout expired: missing " + (nRAResponders - responses.size()) + " responses");
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
										}
										else
										{
											if(setSellingPrice < acceptedSellPriceThreshold)
											{
												reply.setPerformative(ACLMessage.FAILURE);
											}
											else
											{
												reply.setContent(Double.toString(setBuyingPrice));
											}
											
										}
										acceptances.addElement(reply);
									}
								}
								// Accept the proposal of the best proposer
								if (accept != null)
								{
									System.out.println("Accepting proposal " + bestProposal + " from responder "
											+ bestProposer.getName());
									accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
								}
							}

							protected void handleInform(ACLMessage inform)
							{
								System.out.println("Agent " + inform.getSender().getName()
										+ " successfully performed the requested action");
								setSellingPrice = originalSellingPrice;
							}
						});
					}
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
}
