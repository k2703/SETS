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
    	ServiceDescription sd = new ServiceDescription();
    	sd.setType("home");
    	sd.setName(getLocalName());
    	register(sd);
    	
    	DFAgentDescription[] retailers = searchDF("retailer");
    	DFAgentDescription[] appliances = searchDF("appliance");
    	
    	pingDF(retailers);
    	pingDF(appliances);
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
    
    DFAgentDescription[] searchDF(String aType) {
    	DFAgentDescription[] result = null;	
    	try {
	    	DFAgentDescription dfd = new DFAgentDescription();  	
	    	ServiceDescription sd = new ServiceDescription();
			sd.setType( aType );
			dfd.addServices(sd);
			
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
    
    void pingDF(DFAgentDescription[] aRecipients) {
    	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    	msg.setContent("Ping");
    	for (int i = 0; i < aRecipients.length; i++) {
    		msg.addReceiver(aRecipients[i].getName());
    	}
    	send(msg);
    	
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

