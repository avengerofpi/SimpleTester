Summary:
  This project provides a simple testing routine written in Java. The
premise is that you have a properties file (more multiple such files)
containing a mapping from some list of prompts to some list of values.
For example, mapping States to their Capitals, or vice versa. This
program asks you to select the properties file you want to test against
(if there is more than one such file found), allows you to select the
number of prompts you actually want to test against (if there are "a
lot" in the chosen file), and then randomly works through the list of
prompts testing your input against the expected result. The test is
punctuation sensitive (so "St Paul" is not the same as "St. Paul") but
is not case-sensitive (so "st pAUl", "sT PaUl", "ST PAUl", etc. are all
the same). The program allows you to keep retesting against all incorr-
ectly answered prompts until either all have been correctly or you
choose to stop. The program also allows you to restart the testing from
the beginning once you are done with one round (whether by quitting or
by getting all prompts eventually correct).

What do I mean by "a lot":
  This is a hard-coded value, currently = 50

How the code identifies properties files:
  The directory that gets searched is hard-coded, and search is done
for files ending in ".props"

Compiling:
  javac simpleTester.java

Running:
  java simpleTester

