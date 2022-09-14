Feature: Create a delay before any step

  Some systems have a characteristic known as eventual
  consistency. This is common in distributed computing 
  systems such as those following a microservice architecture.

  In basic terms, once data is written and the system 
  continues to function, subsequent reads of that data
  will eventually return the same value. This may mean
  that we need to create a delay at some point within a
  scenario to allow the data to become consistent.

  The "eventually" step takes a single argument which is
  the next step to execute. It then creates a delay 
  before executing the specified step. 

  Given Trevor is a user
  And Trevor creates users from the file './features/data/wip/login/valid-user.json' in the test service
  And eventually data becomes consistent