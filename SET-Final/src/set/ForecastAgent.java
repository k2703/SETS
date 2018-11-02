package set;
import jade.core.Agent;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREResponder;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
public class ForecastAgent extends Agent
{
	private String name;
	private postURL a;
	protected void setup()
	{
		Object args[] = getArguments();
		register(args[0].toString(), args[1].toString());
		System.out.println("Agent " + getLocalName() + " waiting for requests...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		a = new postURL("test.csv");
		this.addBehaviour(new AchieveREResponder(this, template)
		{
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException
			{
				System.out.println("Agent " + getLocalName() + ": REQUEST received from "
						+ request.getSender().getName() + ". Action is " + request.getContent());
				name = request.getContent().split(",")[1];
				return null;
			}

			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response)
					throws FailureException
			{
				double data[] = a.UseService(name);
				if (data.length != 0)
				{
					String content = Double.toString(data[0]) + "," + Double.toString(data[1]);
					System.out.println("Agent " + getLocalName() + ": Action successfully performed");
					ACLMessage inform = request.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					inform.setContent(content);
					return inform;
				} else
				{
					System.out.println("Agent " + getLocalName() + ": Action failed");
					throw new FailureException("unexpected-error");
				}
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
