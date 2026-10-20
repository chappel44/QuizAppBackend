package com.tasks.organizer.topic.entity;

public enum Visibility {
  PUBLIC, //Organization made but allowed to be viewed and taken by the public. 
  PRIVATE //Made by an organization but the scope is limited to the students in that organization.
  //If public users test is PRIVATE then it can only be taken by that user.
}
