package com.rns.web.edo.service.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.rns.web.edo.service.bo.api.EdoAdminBo;
import com.rns.web.edo.service.domain.EdoAdminRequest;

@EnableScheduling
public class EdoBackupRoutine implements SchedulingConfigurer {

	@Autowired
	@Qualifier(value = "adminBo")
	private EdoAdminBo adminBo;
	
	public void setAdminBo(EdoAdminBo adminBo) {
		this.adminBo = adminBo;
	}
	
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		
		// Add session clean up task
				taskRegistrar.addTriggerTask(new Runnable() {
					public void run() {
						EdoAdminRequest request = new EdoAdminRequest();
						EdoAdminRequest lastDate = adminBo.getLastUplinkDate();
						if(lastDate != null) {
							request.setDate(lastDate.getDate());
						} else {
							Calendar cal = Calendar.getInstance();
							cal.set(Calendar.HOUR, 0);
							cal.set(Calendar.MINUTE, 0);
							request.setDate(cal.getTime());
						}
						LoggingUtil.logMessage("Starting uplink task ...... " + request.getDate());
						adminBo.uplinkData(request);
					}
				}, new Trigger() {
					public Date nextExecutionTime(TriggerContext arg0) {
						return nextExecution();
					}

					private Date nextExecution() {
						String frequency = EdoPropertyUtil.getProperty(EdoPropertyUtil.UPLINK_FREQUENCY);
						if(StringUtils.isNotBlank(frequency)) {
							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.MINUTE, new Integer(frequency));
							Date time = cal.getTime();
							LoggingUtil.logMessage("Next execution will start at .. " + time);
							return time;
						}
						return null;
					}
				});

	}

}
