package org.hknu.healthcare.entity;

public enum enums {;

    public enum Gender { MALE, FEMALE, OTHER }
    public enum RelationRole { OWNER, PARENT, CHILD, GRANDPARENT, PARTNER, OTHER }
    public enum InvitationStatus { PENDING, ACCEPTED, REJECTED, EXPIRED }
    public enum AppointmentStatus { SUGGESTED, RESERVED, CONFIRMED, CANCELED, COMPLETED }
    public enum ReviewRating { ONE, TWO, THREE, FOUR, FIVE }
    public enum MissionType { DAILY, WEEKLY, SEASONAL }
    public enum MissionStatus { ASSIGNED, IN_PROGRESS, DONE, EXPIRED }
    public enum RewardTxnType { EARN, SPEND, ADJUST }
    public enum IntakeUnit { TAB, CAP, ML, MG, DROP, PATCH, OTHER }
    public enum ReminderStatus { ACTIVE, SNOOZED, STOPPED }

}
