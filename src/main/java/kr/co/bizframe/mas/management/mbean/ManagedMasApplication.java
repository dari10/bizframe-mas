package kr.co.bizframe.mas.management.mbean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.api.management.mbean.ManagedCamelContextMBean;
import org.apache.camel.management.mbean.ManagedCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.co.bizframe.mas.Application;
import kr.co.bizframe.mas.Serviceable;
import kr.co.bizframe.mas.application.MasApplication;
import kr.co.bizframe.mas.camel.CamelApplication;
import kr.co.bizframe.mas.core.MasServer;
import kr.co.bizframe.mas.management.JMXManager;
import kr.co.bizframe.mas.process.JVMApplication;

public class ManagedMasApplication extends NotificationBroadcasterSupport implements  ManagedMasApplicationMBean {
	
	private static Logger log = LoggerFactory.getLogger(ManagedMasApplication.class);

	private MasApplication mapplication;
	
	private long sequenceNumber = 1;
	
	public ManagedMasApplication(MasApplication application){
		this.mapplication = application;
	}
	
	@Override
	public String getName(){
		return mapplication.getContext().getName();
	}
	
	@Override
	public Date getInitTime(){
		long initTime = mapplication.getInitTime();
		return new Date(initTime);
	}
	
	
	@Override
	public Date getStartTime(){
		long startTime = mapplication.getStartTime();
		return new Date(startTime);
	}
	
	
	@Override
	public Date getStopTime(){
		long stopTime = mapplication.getStopTime();
		return new Date(stopTime);
	}
	
	@Override
	public String getStatus(){
		return mapplication.getStatus().toString();
	}
	
	@Override
	public String getApplicationType(){
		return mapplication.getContext().getApplicationDef().getLoadClass();
	}
	
	@Override
	public String getApplicationPath(){
		return mapplication.getContext().getContextDir();
	}
	
	@Override
	public List<String> getSubManagementNames(){
		try{
			Application app = mapplication.getApplication();
			if(app instanceof CamelApplication){
				return getCamelSubManagementNames(app);
			}
		}catch(Throwable t){
			log.error(t.getMessage());
		}	
		
		return null;
	}
	
	@Override
	public Map<String, String> getParameters(){
		Map<String, Object> ps = mapplication.getContext().getProperties();
		Map<String, String> v = new HashMap<String, String>();
		for(String key : ps.keySet()){
			v.put(key, ps.get(key).toString());
		}
		return v;
	}
	
	@Override
	public int getPriority(){
		return mapplication.getContext().getApplicationDef().getPriority();
	}
	
	@Override
	public boolean getServiceable(){
		Application app = mapplication.getApplication();
		if(app instanceof Serviceable){
			return true;
		}
		return false;
	}
	
	@Override
	public void start() throws Exception {
		try{
			mapplication.getContext().getApplicationManager().
			startApplication(mapplication.getContext().getId());
		}catch(Throwable t){
			log.error(t.getMessage(), t);
			throw new Exception(t.getMessage());
		}
	}

	@Override
	public void stop() throws Exception {
		try{
			mapplication.getContext().getApplicationManager().
			stopApplication(mapplication.getContext().getId());
		}catch(Throwable t){
			log.error(t.getMessage(), t);
			throw new Exception(t.getMessage());
		}
	}
	
	@Override
	public void undeploy() throws Exception {
		try{
			mapplication.getContext().getApplicationManager().
			undeployApplication(mapplication.getContext().getId());
		}catch(Throwable t){
			log.error(t.getMessage(), t);
			throw new Exception(t.getMessage());
		}
	}

	
	@Override
	public void deploy() throws Exception {
		try{
			mapplication.getContext().getApplicationManager().
			deployApplication(mapplication.getContext().getId());
		}catch(Throwable t){
			log.error(t.getMessage(), t);
			throw new Exception(t.getMessage());
		}
	}
	
	
	private List<String> getCamelSubManagementNames(Application app){
		List<String> result = new ArrayList<String>();
		CamelApplication ca = (CamelApplication)app;
		List<CamelContext> ccs = ca.getCamelContexts();
		for(CamelContext context : ccs){
			ManagedCamelContextMBean mcc = context.getManagedCamelContext();
			if(mcc != null){
				String id = mcc.getManagementName();
				result.add(id);
			}
		}
		return result;
	}
	
	
	public synchronized void changeStatusNotification(MasApplication.Status preStatus,
			MasApplication.Status status){
		
		Notification n = new AttributeChangeNotification(this, sequenceNumber++, 
				System.currentTimeMillis(),
				"Status changed", 
				"Status", 
				"String", 
				preStatus.toString(), 
				status.toString());

		sendNotification(n);
	}
	
	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		String[] types = new String[] { AttributeChangeNotification.ATTRIBUTE_CHANGE };

		String name = AttributeChangeNotification.class.getName();
		String description = "Mas Application status has changed";
		MBeanNotificationInfo info = new MBeanNotificationInfo(types, name, description);
		return new MBeanNotificationInfo[] { info };
	}	
	
}
