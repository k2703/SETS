package agentspackage;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAException;

public class RetailAgent extends Agent {
	final int EXPECTED_ARG_COUNT = 2;
	
	enum PricingMechanism {
		// random initial price
		RANDOM,
		// TODO add and implement additional mechanisms below
	}
	
	enum NegotiationStrategy {
		// reduce by 10% for each iteration
		REDUCE10,
		// TODO add and implement additional strategies below
	}
	
	
    protected void setup() 
    {
    	// retail agent requires pricing mechanism argument
    	Object[] args = getArguments();
    	if (args != null && args.length == EXPECTED_ARG_COUNT) {
    		// initialize mechanism and strategy
    		PricingMechanism selectedMechanism;
    		NegotiationStrategy selectedStrategy;
    		
    		// first agent argument is mechanism
    		if (args[0].equals("random")) {
    			selectedMechanism = PricingMechanism.RANDOM;
    		}
    		else {
    			System.err.println("Input Pricing Mechanism '" + args[0] + "' not implemented!");
    			System.out.println("Terminating Agent " + getLocalName());
    			doDelete();
    		}
    		
    		// second agent argument is strategy
    		if (args[1].equals("reduce10")) {
    			selectedStrategy = NegotiationStrategy.REDUCE10;
    		}
    		else {
    			System.err.println("Input Negotiation Strategy '" + args[1] + "' not implemented!");
    			System.out.println("Terminating Agent " + getLocalName());
    			doDelete();
    		}
    		
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
    	else {
    		System.err.println("Failed to create RetailAgent '" + getLocalName() + "'");
    		if (args != null) {
    			System.out.println(args.length + "/" + EXPECTED_ARG_COUNT + " arguments were included.");;
    		} else {
    			System.out.println("0/" + EXPECTED_ARG_COUNT + " arguments were included.");
    		}
    	}
    	
    	
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

