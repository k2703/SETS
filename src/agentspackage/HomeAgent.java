package agentspackage;

import jade.core.Agent;

import jade.core.AID;
import jade.core.behaviours.*;

import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

import jade.lang.acl.*;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class HomeAgent extends Agent {
	// TODO figure out if this enum is pointless
	// I'm just using it to prevent issues from typos when sending messages
	enum MessageContents {
		REQUEST_NEGOTIATION,
		AGREE_NEGOTIATION,
	}
	
    protected void setup() 
    {
    	// create a new service description for this Home
    	ServiceDescription sd = new ServiceDescription();
    	sd.setType("home");
    	sd.setName(getLocalName());
    	// register it to the DF service
    	register(sd);
    	
    	// search the DFService for retailers and appliances
    	// store them in separate arrays
    	DFAgentDescription[] retailers = searchDF("retailer");
    	DFAgentDescription[] appliances = searchDF("appliance");
    	
    	// ping the retailers, then ping the appliances (FOR TESTING)
    	//pingDF(retailers);    	
    	//pingDF(appliances);
    	
    	int powerRequired = findPowerRequired(appliances);
    	// placeholder
    	int startingPrice = 10;
    	requestPower(retailers, powerRequired, startingPrice);
    }
    
    
    void requestPower(DFAgentDescription[] retailers, int powerRequired, int startingPrice) {
    		
    	// create an ACL message to ask for power
    	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    	// TODO send message to begin negotiation with retail agents
    	msg.setContent(MessageContents.REQUEST_NEGOTIATION.name());
    	System.out.println(getLocalName() + ": sending negotiation req to:");
    	for (int i = 0; i < retailers.length; i++) {
    		System.out.println("\t" + retailers[i].getName());
    		msg.addReceiver(retailers[i].getName());
    	}
    	send(msg);
    	
    	// add a new ticker behaviour which waits for a response
    	addBehaviour(new TickerBehaviour(this, 1000) 
    	{
    		int i;
    		ArrayList<ACLMessage> received = new ArrayList<ACLMessage>();
    		public void onStart() {
    			i = 0;
    		}
    		public void onTick() {
    			ACLMessage msg = receive();
    			if (msg!=null) {
    				i++;
    				System.out.println(getLocalName() + ": received answer '" + msg.getContent() + "' from " + msg.getSender().getName());
    				received.add(msg);
    			}
    			if (i >= retailers.length) {
    				System.out.println(getLocalName() + ": received responses from all agents.");
    				stop();
    			}
    		}
    		public int onEnd() {
    			startNegotiation(received, startingPrice);
    			return 0;
    		}
    	});
    	
    	addBehaviour(new TickerBehaviour(this, 1000)
    	{
    		// TODO fix this
    		int currentLowest = 99999999;
    		public void onTick() {
    			ACLMessage msg = receive(), reply;
    			if (msg!=null) {
    				System.out.println(getLocalName() + ": received answer '" + msg.getContent() + "' from " + msg.getSender().getName());
            		System.out.println("Starting price: " + startingPrice);
            		System.out.println("Proposed price: " + msg.getContent());
    				currentLowest = comparePrice(msg, currentLowest);
    				if (currentLowest <= startingPrice) {
        				stop();
        			}
        			else {
        				reply = msg.createReply();
            			reply.setPerformative(ACLMessage.REFUSE);
            			send(reply);
        			}
    			}
    			
    		}
    		public int onEnd() {
    			System.out.println(getLocalName() + ": accepting price of " + currentLowest);
    			ACLMessage reply = msg.createReply();
    			reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
    			send(reply);
    			return 0;
    		}
    	});    	
    }
    
	int comparePrice(ACLMessage msg, int currentLowest) {
		int result;
		if (Integer.parseInt(msg.getContent()) < currentLowest) {
			result = Integer.parseInt(msg.getContent());
		} else {
			result = currentLowest;
		}
		
		return result;
	}
    
    
    void startNegotiation(ArrayList<ACLMessage> received, int startingPrice) {
    	ACLMessage reply;
		
		for (ACLMessage msg : received) {
    		System.out.println("Starting price: " + startingPrice);
    		System.out.println("Proposed price: " + msg.getContent());
    		if (Integer.parseInt(msg.getContent()) <= startingPrice) {
    			System.out.println(getLocalName() + ": accepting price of " + msg.getContent());
    			reply = msg.createReply();
    			reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
    			send(reply);
    			// break out of the for loop? should wait to see if someone is offering a lower price this round
    			break;
    		} else {
    			System.out.println(getLocalName() + ": refusing price of " + msg.getContent());
    			reply = msg.createReply();
    			reply.setPerformative(ACLMessage.REFUSE);
    			send(reply);
    		}
    	}	
    }
    
    // TODO this is unused, needs to be generated from ApplianceAgent?
    int findPowerRequired(DFAgentDescription[] appliances) {
    	// TODO loop through all appliances and estimate amount of power required?
    	// just return a random one for now
    	return ThreadLocalRandom.current().nextInt(100, 1000 + 1);
    }
    
    // function for registering the Home to the DFService
    void register(ServiceDescription sd)
    {
    	// create a new DFAgentDescription
    	DFAgentDescription dfd = new DFAgentDescription();
    	dfd.setName(getAID());
    	// pass our description of this Home DFAgentDesc
    	dfd.addServices(sd);
    	
    	try {
    		// register THIS Home with our constructed description
    		DFService.register(this, dfd);
    	}
    	catch (FIPAException fe) {
    		fe.printStackTrace();
    	}
    }
    
    // search the DFService for a type of service
    DFAgentDescription[] searchDF(String aType) {
    	DFAgentDescription[] result = null;	
    	try {
    		// set the search parameters
	    	DFAgentDescription dfd = new DFAgentDescription();  	
	    	ServiceDescription sd = new ServiceDescription();
			sd.setType( aType );
			dfd.addServices(sd);
			
			// search based on the above parameters
			result = DFService.search(this, dfd);
			if (result==null) System.out.println(getLocalName() + ": Search for " + aType + " returns null");
			else {
				System.out.println(getLocalName() + ": Search for " + aType + ": " + result.length + " elements" );
				if (result.length>0) {
					for (int i = 0; i < result.length; i++) {
						System.out.println(" " + result[i].getName() );
					}
				}
			}
    	}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return result;
    }
    
    // ping a list of agents
    void pingDF(DFAgentDescription[] aRecipients) {
    	// create the ACL message and set the message content
    	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    	msg.setContent("Ping");
    	// construct the list of recipients
    	for (int i = 0; i < aRecipients.length; i++) {
    		msg.addReceiver(aRecipients[i].getName());
    	}
    	// send the message
    	send(msg);
    	
    	// add a new ticker behaviour which waits for a response
    	addBehaviour(new TickerBehaviour(this, 1000) 
    	{
    		int i;
    		public void onStart() {
    			i = 0;
    		}
    		public void onTick() {
    			ACLMessage msg = receive();
    			if (msg!=null) {
    				i++;
    				System.out.println(getLocalName() + ": received answer '" + msg.getContent() + "' from " + msg.getSender().getName());
    			}
    			if (i >= aRecipients.length) {
    				System.out.println(getLocalName() + " : received responses from all agents.");
    				stop();
    			}
    		}
    	});
    	
    }
}

