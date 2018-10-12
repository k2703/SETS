package agentspackage;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAException;

public class RetailAgent extends Agent {
	
    protected void setup() 
    {
    	// create a new service description for this Retailer
    	ServiceDescription sd = new ServiceDescription();
    	sd.setType("retailer");
    	sd.setName(getLocalName());
    	// register it to the DFService
    	register(sd);
    	
    	// add a CyclicBehaviour which waits for an ACL message to be received  
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
    
    // function for registering the Retailer to the DFService
    void register(ServiceDescription sd)
    {
    	// create a new DFAgentDescription
    	DFAgentDescription dfd = new DFAgentDescription();
    	dfd.setName(getAID());
    	// pass our description of this Retailer DFAgentDesc
    	dfd.addServices(sd);
    	
    	try {
    		// register THIS Retailer with our constructed description
    		DFService.register(this, dfd);
    	}
    	catch (FIPAException fe) {
    		fe.printStackTrace();
    	}
    }
}

