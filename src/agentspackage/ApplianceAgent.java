package agentspackage;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAException;

public class ApplianceAgent extends Agent {
	
    protected void setup() 
    {
    	ServiceDescription sd = new ServiceDescription();
    	sd.setType("appliance");
    	sd.setName(getLocalName());
    	register(sd);
    	
    	addBehaviour(new CyclicBehaviour(this)
    	{
    		public void action() {
    			ACLMessage msg = receive();
    			if (msg!=null) {
    				System.out.println(getLocalName() + ": received '" + msg.getContent() + "' from " + msg.getSender().getName());
    				ACLMessage reply = msg.createReply();
    				reply.setPerformative(ACLMessage.INFORM);
    				reply.setContent("Pong");
    				send(reply);
    			}
    			block();
    		}
    	});
    }
    
    void register(ServiceDescription sd)
    {
    	DFAgentDescription dfd = new DFAgentDescription();
    	dfd.setName(getAID());
    	dfd.addServices(sd);;
    	
    	try {
    		DFService.register(this, dfd);
    	}
    	catch (FIPAException fe) {
    		fe.printStackTrace();
    	}
    }
}

