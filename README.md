# jms-utils
Utils to remove stale messages from queues and durables.

### Runbook
Application runbook (usage + examples): app/runbook.txt.

### Main goals
1. Drain messages from queue, store them in tmp files, delete such messages from queue, then replay some of them if needed;
2. Do the same with durables.

### Storage files format
- *.inf  
All JMS message info + headers.  
Text message payload or object message toString().    
- *.dat  
ArrayList saved as Serialized object.  
ArrayList consists of MessageWrapper objects:  
messageId, messageType, payload (String or Serializable).

### Usage scenario 1
Load messages into binary file.  
Delete messages from destination.
Get necessary IDs to replay.  
Replay selected messages.  

### Usage scenario 2
Define date to delete stale messages (all messages before this date will be removed).  
Delete messages before defined date.  
If runned from script, deletion date can be defined as shift from launch time  
(all messages older than N days,hours,minutes from current time).


