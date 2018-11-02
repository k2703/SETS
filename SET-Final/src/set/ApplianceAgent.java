package set;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.MessageTemplate;

import java.util.Date;
import java.util.Vector;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;

@SuppressWarnings("serial")
public class ApplianceAgent extends Agent
{
	private double actual;
	private double predicted;
	private String name;
	private postURL a;
	private String tou;
/*	private AID[] forecastProviders;
	private int forecastResponders;
	private SequentialBehaviour seq;
	private Boolean sa = false;*/

	@SuppressWarnings("serial")
	protected void setup()
	{
		Object args[] = getArguments();
		register(args[0].toString(), args[1].toString());
		name = args[0].toString();
		a = new postURL("test.csv");
		/*forecastProviders = getService("forecast");
		forecastResponders = forecastProviders.length;*/
		/*String msg = "predict," + name;*/
		/*ACLMessage getPred = createMessage(forecastResponders, forecastProviders, msg,
				FIPANames.InteractionProtocol.FIPA_REQUEST, ACLMessage.REQUEST);
		// AchieveREInitiator a = new AchieveREInitiator(this, getPred);
		SequentialBehaviour seq = new SequentialBehaviour();*/
		System.out.println("Agent " + getLocalName() + " waiting for requests...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		// EInitiator a = new EInitiator(this, getPred);
		AchieveREResponder b = (new AchieveREResponder(this, template)
		{
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException
			{
				System.out.println("Agent " + getLocalName() + ": REQUEST received from "
						+ request.getSender().getName() + ". Action is " + request.getContent());
				return null;
			}

			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response)
					throws FailureException
			{
				double data[] = a.UseService(name);
				if (data.length != 0)
				{
					actual = data[0];
					predicted = data[1];
					tou = a.UseDate();
					String content = Double.toString(data[0]) + "," + Double.toString(data[1] )+ "," + tou;
					System.out.println("Agent " + getLocalName() + ": Action successfully performed");
					ACLMessage inform = request.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					inform.setContent(content);
					return inform;
				}
				else
				{
					System.out.println("Agent " + getLocalName() + ": Action failed");
					throw new FailureException("unexpected-error");
				}
			}
		});
		addBehaviour(b);
/*		seq.addSubBehaviour(a);
		seq.addSubBehaviour(b);
		CyclicBehaviour z = new CyclicBehaviour()
		{

			@Override
			public void action()
			{
				addBehaviour(seq);

			}

		};
		addBehaviour(z);*/
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

	/*private ACLMessage createMessage(int receivers, AID[] agents, String content, String protocol, int type)
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

	private class EInitiator extends AchieveREInitiator
	{
		public EInitiator(Agent a, ACLMessage msg)
		{
			super(a, msg);
		}

		protected void handleAgree(ACLMessage agree)
		{
			System.out.println(getLocalName() + ": " + agree.getSender().getName() + " has agreed to the request");
		}

		protected void handleInform(ACLMessage inform)
		{
			System.out.println(getLocalName() + ": " + inform.getSender().getName()
					+ " successfully performed the requested action");
			System.out.println(getLocalName() + ": " + inform.getSender().getName() + "'s predicted energy demand is "
					+ inform.getContent());
			String vals[] = inform.getContent().split(",");
			actual += Double.parseDouble(vals[0]);
			predicted += Double.parseDouble(vals[1]);
		}

		protected void handleRefuse(ACLMessage refuse)
		{
			System.out.println(
					getLocalName() + ": " + refuse.getSender().getName() + " refused to perform the requested action.");
			forecastResponders--;
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
			if (notifications.size() < forecastResponders)
			{
				System.out.println(getLocalName() + ": " + "Timeout expired: missing "
						+ (forecastResponders - notifications.size()) + " responses");
			} else
			{
				System.out.println(getLocalName() + ": " + "Received notifications from every responder");
			}
		}
	}*/
}
