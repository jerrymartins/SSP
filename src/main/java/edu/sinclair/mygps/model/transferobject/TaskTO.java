package edu.sinclair.mygps.model.transferobject;

import java.util.Date;
import java.util.UUID;

public class TaskTO {
	public static final String TASKTO_ID_PREFIX_DELIMITER = ":";
	public static final String TASKTO_ID_PREFIX_ACTION_PLAN_TASK = "ACT";
	public static final String TASKTO_ID_PREFIX_CUSTOM_ACTION_PLAN_TASK = "CUS";
	public static final String TASKTO_ID_PREFIX_SSP_ACTION_PLAN_TASK = "SSP";

	private UUID id;
	private String type;
	private String name;
	private String description;
	private String details;
	private Date dueDate;
	private boolean completed;
	private boolean deletable;
	private UUID challengeId;
	private UUID challengeReferralId;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public Date getDueDate() {
		return dueDate == null ? null : new Date(dueDate.getTime());
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate == null ? null : new Date(dueDate.getTime());
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	public UUID getChallengeId() {
		return challengeId;
	}

	public void setChallengeId(UUID challengeId) {
		this.challengeId = challengeId;
	}

	public UUID getChallengeReferralId() {
		return challengeReferralId;
	}

	public void setChallengeReferralId(UUID challengeReferralId) {
		this.challengeReferralId = challengeReferralId;
	}
}
