package agentspackage;

import java.util.concurrent.ThreadLocalRandom;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAException;

public class RetailAgent extends Agent {
	final int EXPECTED_ARG_COUNT = 2;
	
	// initialize mechanism and strategy
	PricingMechanism selectedMechanism;
	NegotiationStrategy selectedStrategy;
	
	// TODO figure out if this enum is pointless
	// I'm just using it to prevent issues from typos when sending messages
	enum MessageContents {
		REQUEST_NEGOTIATION,
		AGREE_NEGOTIATION
	}
	
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
    	if (args != null && args.length == EXPECTED_ARG_COUNT && checkArgs((String[])args)) {
   		
    		// first agent argument is mechanism
    		if (args[0].equals("random")) {
    			selectedMechanism = PricingMechanism.RANDOM;
    		}
    		
    		// second agent argument is strategy
    		if (args[1].equals("reduce10")) {
    			selectedStrategy = NegotiationStrategy.REDUCE10;
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
        		int price = 0;
        		public void action() {
        			ACLMessage reply;
        			ACLMessage msg = receive();
        			if (msg!=null) {
        				System.out.println(getLocalName() + ": received '" + ACLMessage.getPerformative(msg.getPerformative()) + " - " + msg.getContent() + "' from " + msg.getSender().getName());
        				
        				// if home is requesting to negotiate
        				if (msg.getPerformative() == ACLMessage.REQUEST && msg.getContent().equalsIgnoreCase(MessageContents.REQUEST_NEGOTIATION.toString())) {
        					reply = msg.createReply();
        					reply.setPerformative(ACLMessage.AGREE);
        					if (selectedMechanism == PricingMechanism.RANDOM) {
        						price = ThreadLocalRandom.current().nextInt(100, 1000 + 1);
        						reply.setContent(Integer.toString(price));
        					}
             				send(reply);
        				}
        				
        				// if home has refused an offer
        				if (msg.getPerformative() == ACLMessage.REFUSE) {
        					if (msg.getContent() == null) {
        						reply = msg.createReply();
        						if (selectedStrategy == NegotiationStrategy.REDUCE10) {
        							price -= price/10;
        							System.out.println(getLocalName() + ": reducing price by 10%...");
        							reply.setContent(Integer.toString(price));
        							send(reply);
        						}
        						
        					}
        				}
        				
        			}
        			block();
        		}
        	});
        	
        	
    	}
    	else {
    		System.err.print("Failed to create RetailAgent '" + getLocalName() + "': ");
    		if (args != null){
    			if (args.length != EXPECTED_ARG_COUNT) {
    				System.err.println(args.length + "/" + EXPECTED_ARG_COUNT + " arguments were included.");;
    			}
    			else {
    				System.err.println("Invalid argument provided.");
    			}
    		} 
    		else {
    			System.err.println("0/" + EXPECTED_ARG_COUNT + " arguments were included.");
    		}
    		doDelete();
    	}
    	
    	
    	
    	
    	
    }
    
    Boolean checkArgs(String[] args) {
    	Boolean testPM = false, testNS = false;
    	for (PricingMechanism pm : PricingMechanism.values()) {
    		if (args[0].compareToIgnoreCase(pm.name()) == 0) {
    			testPM = true;
    		}
    	}
    	
    	for (NegotiationStrategy ns : NegotiationStrategy.values()) {
    		if (args[1].compareToIgnoreCase(ns.name()) == 0) {
    			testNS = true;
    		}
    	}
    	
    	return (testPM && testNS);
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

