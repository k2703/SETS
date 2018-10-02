package agentspackage;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

public class ApplianceAgent extends Agent {
	
    protected void setup() 
    {
    	ServiceDescription sd = new ServiceDescription();
    	sd.setType("appliance");
    	sd.setName(getLocalName());
    	register(sd);
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

