package agentspackage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;

import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

import jade.lang.acl.*;

public class HomeAgent extends Agent {
	
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
    	pingDF(retailers);
    	pingDF(appliances);
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
    	
    	// add a new cyclic behaviour which waits for a response
    	addBehaviour(new CyclicBehaviour(this)
    	{
    		public void action() {
    			ACLMessage msg = receive();
    			if (msg!=null) {
    				System.out.println(getLocalName() + ": received answer '" + msg.getContent() + "' from " + msg.getSender().getName());
    			}
    			block();
    		}
    	});
    }
}

