:: To automatically start programs at a time 
::First save this script
:: Open Task Scheduler by entering taskschd.msc in the Run dialog. When Task Scheduler opens, create a new Task, and give it a name. In Triggers tab, configure the scheduled time, in actions select this batch script to run.

@echo off ::@echo off is used  stop echo of commands
::The rem command is just a comment to identify the program, CD command changes the directory, and start is, well it just starts the program.

rem open a website
jupyter notebook

