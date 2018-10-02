package agentspackage;

import jade.core.Agent;
import jade.core.AID;

import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

public class HomeAgent extends Agent {
	
    protected void setup() 
    {
    	ServiceDescription sd = new ServiceDescription();
    	sd.setType("home");
    	sd.setName(getLocalName());
    	register(sd);
    	
    	searchDF("retailer");
    	searchDF("appliance");
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
    
    void searchDF(String aType) {
    	try {
	    	DFAgentDescription dfd = new DFAgentDescription();  	
	    	ServiceDescription sd = new ServiceDescription();
			sd.setType( aType );
			dfd.addServices(sd);
			
			DFAgentDescription[] result = DFService.search(this, dfd);
			if (result==null) System.out.println("Search for " + aType + " returns null");
			else {
				System.out.println("Search for " + aType + ": " + result.length + " elements" );
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
    }
}

