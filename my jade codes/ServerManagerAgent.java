import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.wrapper.*;
import javax.swing.*;
import java.util.*;

public class ServerManagerAgent extends Agent
{
	int ID,num_of_vms; 
	public void setup()
	{
		setEnabledO2ACommunication(true,0);
		Object[] args = getArguments();
		ID = (Integer)args[0];
		num_of_vms = (Integer)args[1];
		// System.out.println(getLocalName()+" with ID "+ID+" is started.(No. of vms => "+num_of_vms+")");
		addBehaviour(new RequestGetter());
 	}
 	class RequestGetter extends CyclicBehaviour
 	{
 		MessageTemplate msgtemplate;
 		String[] strarr;
 		public void action()
 		{
 			msgtemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchOntology("requesting-for-capacity"));
 			ACLMessage msg = receive(msgtemplate);
 			if(msg!=null)
 			{
 				System.out.println("Requesting for capacity message has been received to sma "+ID);
 				
 				//requesting VMs
 				ACLMessage msg2vma = new ACLMessage(ACLMessage.REQUEST);
 				msg2vma.setOntology(msg.getOntology());
 				for(int i=1;i<=num_of_vms;i++)
 					msg2vma.addReceiver(new AID("vma_"+ID+"_"+i,AID.ISLOCALNAME));
 				send(msg2vma);
 				System.out.println("requesting-for-capacity message -> from SMA"+ID+" to all its VMs");

 				//receiving replies from all the VMAs
 				/*int count = 0;
 				msgtemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchOntology("requesting-for-capacity"));
 				strarr = new String[num_of_vms];
 				while(count < num_of_vms)
 				{
 					//waiting for a message from vma
 						while((msg = receive(msgtemplate)) == null)
 						{
 							;
 						}
 						strarr[count] = msg.getContent();
 						count++;
 				}*/	
 				int count = 0;
 				ArrayList<VirtualMachine> vmarray = new ArrayList<VirtualMachine>();
 				Object obj;
 				while(count < num_of_vms)
 				{
 					while((obj = getO2AObject()) == null)
 						;
 					if(obj.getClass().getSimpleName().equals("VirtualMachine"))
 					{
 						vmarray.add((VirtualMachine)obj);
 						count++;
 					}
 				}

 				//replying to ClusteringAgent
 				try
 				{
					jade.wrapper.AgentContainer container = getContainerController(); 
					AgentController agentcont = container.getAgent("ca"); //ContainerController's method - getAgent()
					//agentcont.putO2AObject(strarr,false);
					agentcont.putO2AObject(vmarray,false);
					System.out.println("Capacity info - sent by "+getLocalName()+" to CA");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
 				/*ACLMessage reply = msg.createReply();
 				reply.setPerformative(ACLMessage.INFORM);
 				// reply.setOntology("requesting-for-capacity");
 				reply.setContent();*/
 			}
 		}
 	}
}