# Homework 3
### Description: object-oriented pure functional design and implementation of an external command execution framework as an I/O monad.
### Grade: 11%
#### You can obtain this Git repo using the command ```git clone git@bitbucket.org:cs474_fall2019/homework3.git'''.
#### Preliminaries are the same as in the previous homeworks, except doing this homework in Scala is mandatory.

## Overview and Functionality
The goal of this homework is to gain experience with pure functional object-oriented design of a practically useful and important framework for composing and executing external commands and applications from Scala programs and obtaining and processing the results of these executions. This homework is based on the material from the textbook on Functional Programming in Scala by Paul Chiusano and Rúnar Bjarnason and it is modeled on the pure functional design principles described in sections 6 and 7 of the textbook.

A large number of software applications use commands, operations of the underlying OSes as well as other applications installed on these OSes to reuse their functionalities in the implementation of the required business logic. Prof. John Ousterhout stated his observation two decaded ago in his paper on [Integration: A New Style of Programming](https://ieeexplore.ieee.org/document/762796) that many software applications or their parts are created by integrating existing software resources. Having worked with large-scale applications on 30+ software projects at 25+ companies, I observed that programmers often reuse external scripts and commands as well as other executable applications to implement parts of software requirements in Java, C#, and Scala applications. Consider a frequent pipeline of Unix commands where the command `ssh` is used to log into a remote host, then listing all processes that are currently run on it, finding the processes that are run by **drmark** and terminating these processes. The returning values from these commands is the list of processes and their usage attribute values (e.g., CPU and RAM usage) for further processing. Clearly, everyone who is familiar with Unix commands will construct the following pipeline: ```ps aux >&1 | grep drmark | awk '{print $2}' | xargs kill'''. A different example of downloading a video from youtube and saving it into a file can be accomplished by the pipelined commands ```youtube-dl <videourl> -q -o - | ffmpeg -i - <filepath>'''. The compactness and the expressiveness of these examples shows the power of using external commands from your programs.

Unfortunately, using these commands from Java or Scala programs is far from straightforward. For example, [Baeldung tutorial](https://www.baeldung.com/run-shell-command-in-java) provides the following example.
```java
Process process;
String homeDirectory = System.getProperty("user.home");
if (isWindows) {
    process = Runtime.getRuntime().exec(String.format("cmd.exe /c dir %s", homeDirectory));
} else {
    process = Runtime.getRuntime().exec(String.format("sh -c ls %s", homeDirectory));
}
StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
Executors.newSingleThreadExecutor().submit(streamGobbler);
int exitCode = process.waitFor();
assert exitCode == 0;
```
As you can see, there is nontrivial usage of the JDK to spawn off an external process and receive results via side-effects. Every time a programmer need to run external commands, s/he writes them in string variables or constants. If s/he makes a mistake in the name of the command or the syntax of the command line, the Java/Scala compiler will not detect it. And every programmer must repeat the construction of the commands and test for its success. Then, the programmer must write the code to retrieve the resulting data and stash them in some storage, a variable or a container thus producing side effects. Then, the programmer must write code to process the data, extract some values, and pass them to a newly constructed command string to execute externally. The code grows exceedingly complex, error-prone and difficult to maintain and evolve.

In your third and the final homework you will create an extensible framework with typed external commands that the clients will execute using such monadic combinators as **flatMap**, **Option**, and **Future** among the others. Each external command type will be implemented using the pattern [Builder](https://en.wikipedia.org/wiki/Builder_pattern). Additional 2% bonus will be given if you implement this pattern using [phantom types](https://medium.com/@maximilianofelice/builder-pattern-in-scala-with-phantom-types-3e29a167e863). Here is a general outline for the code that a programmer will write to execute external commands using your framework.
```scala
val sshObject = (new SshCommand).Builder().setHost("hostName").setCredentials(NOPASSWORD).build()
val result = sshObject.flatMap((new PsCommand()).setProcesses(ALLPROC).setProcUser().setProcNotAttached2Term().build()).filter((new User("drmark"))).filter(Column(2)).flatMap(KillCommand())
```

As you can see, your homework can be broken down in the following three phases. In the first phase, you will select what subset of external commands you choose to support and how you will design the type system to hide the complexity of constructing these commands with proper command line parameters and the external use values (e.g., a hostName). Keep in mind that your design must provide sufficient information hiding and allow programmers to extend it at the same time (i.e., you may consider the use of sealed traits). Executing each command results in data, so you will design data parsers for specific commands in which you will embed the information of how the resulting data is structured. For example, the execution of the command `ps aux` results in the following response.
```
$ ps aux
USER       PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
root         1  0.0  0.0  51120  2796 ?        Ss   Dec22   0:09 /usr/lib/systemd/systemd --system
root         2  0.0  0.0      0     0 ?        S    Dec22   0:00 [kthreadd]
root         3  0.0  0.0      0     0 ?        S    Dec22   0:04 [ksoftirqd/0]
root         5  0.0  0.0      0     0 ?        S<   Dec22   0:00 [kworker/0:0H]
root         7  0.0  0.0      0     0 ?        S    Dec22   0:15 [migration/0]
root         8  0.0  0.0      0     0 ?        S    Dec22   0:00 [rcu_bh]
root         9  0.0  0.0      0     0 ?        S    Dec22   2:47 [rcu_sched]
```
It would be unreasonable to expose the user of your framework to this table data. You will provide combinators like `filter(_.equalTo(Column(3)) && _.equalTo(Column(4)))` that will return the projection of the table, specifically the results of the CPU and RAM usage. Thus, the knowledge of the resulting data structure/format will be encapsulated in the command that you design and implement.

In the second phase, you will create your underlying implementation of the actual command executor and data retriever that will be buried in your framework, i.e., the user of your framework will not be exposed to the complexities of the actual interactions with external commands and raw data. Finally, in the third phase of the homework, you will test your framework and you will create examples and documentation for your users that will be included in your submission along with the design document that describes a model of your external command execution, its constraints and rules, your design of the classes, traits, and monadic combinators. Of course, your document should start with example program(s) that you write that use your framework, which also serve as the test cases that verify its behavior.

As always, I wrote this homework script using a retroscripting technique, in which the homework outlines are generally and loosely drawn, and the individual students improvise to create the implementation that fits their refined objectives. In doing so, students are expected to stay within the basic requirements of the homework and they are free to experiments. That is, it is impossible that two non-collaborating students will submit similar homeworks! Asking questions is important, so please ask away at Piazza!

## Baseline
To be considered for grading, your project should be implemented in Scala and it should be buildable using the SBT, and your documentation must include your design and model, the reasoning about pros and cons, explanations of your implementation and the chosen commands with their inputs, constraints and rules, and the results of your runs, and the explanations of your error/warning messages. Simply basic low-level code for executing external commands will result in desk-rejecting your submission.

## Piazza collaboration
You can post questions and replies, statements, comments, discussion, etc. on Piazza. For this homework, feel free to share your ideas, mistakes, code fragments, commands from scripts, and some of your technical solutions with the rest of the class, and you can ask and advise others using Piazza on where resources and sample programs can be found on the internet, how to resolve dependencies and configuration issues. When posting question and answers on Piazza, please select the appropriate folder, i.e., hw3 to ensure that all discussion threads can be easily located. As before, active participants and problem solvers will receive bonuses from the big brother :-) who is watching your exchanges on Piazza (i.e., your class instructor and your TA). However, *you must not describe your design or specific details related how your construct your models!*

## Git logistics
**This is an individual homework.** Separate repositories will be created for each of your homeworks and for the course project. As usual, you will fork this repository and your fork will be private, no one else besides you, the TA and your course instructor will have access to your fork. Please remember to grant a read access to your repository to your TA and your instructor. In future, for the team homeworks and the course project, you should grant the write access to your forkmates, but NOT for this homework. You can commit and push your code as many times as you want. Your code will not be visible and it should not be visible to other students (except for your forkmates for a team project, but not for this homework). When you push the code into the remote repo, your instructor and the TA will see your code in your separate private fork. Making your fork public or inviting other students to join your fork for an individual homework will result in losing your grade. For grading, only the latest push timed before the deadline will be considered. **If you push after the deadline, your grade for the homework will be zero**. For more information about using the Git and Bitbucket specifically, please use this [link as the starting point](https://confluence.atlassian.com/bitbucket/bitbucket-cloud-documentation-home-221448814.html). For those of you who struggle with the Git, I recommend a book by Ryan Hodson on Ry's Git Tutorial. The other book called Pro Git is written by Scott Chacon and Ben Straub and published by Apress and it is [freely available](https://git-scm.com/book/en/v2/). There are multiple videos on youtube that go into details of the Git organization and use.

Please follow this naming convention while submitting your work : "Firstname_Lastname_hw3" without quotes, where you specify your first and last names **exactly as you are registered with the University system**, so that we can easily recognize your submission. I repeat, make sure that you will give both your TA and the course instructor the read/write access to your *private forked repository* so that we can leave the file feedback.txt with the explanation of the grade assigned to your homework.

## Discussions and submission
As it is mentioned above, you can post questions and replies, statements, comments, discussion, etc. on Piazza. Remember that you cannot share your code and your solutions privately, but you can ask and advise others using Piazza and StackOverflow or some other developer networks where resources and sample programs can be found on the Internet, how to resolve dependencies and configuration issues. Yet, your implementation should be your own and you cannot share it. Alternatively, you cannot copy and paste someone else's implementation and put your name on it. Your submissions will be checked for plagiarism. **Copying code from your classmates or from some sites on the Internet will result in severe academic penalties up to the termination of your enrollment in the University**. When posting question and answers on Piazza, please select the appropriate folder, i.e., hw3 to ensure that all discussion threads can be easily located.


## Submission deadline and logistics
Wednesday, November 20 at 11PM CST via the bitbucket repository. Your submission will include the code for the program, your documentation with instructions and detailed explanations on how to assemble and deploy your program along with the results of your verification runs and a document that explains these results based on the your rules and constraints, and what the limitations of your implementation are. Again, do not forget, please make sure that you will give both your TAs and your instructor the read access to your private forked repository. Your name should be shown in your README.md file and other documents. Your code should compile and run from the command line using the commands **sbt clean compile test** and **sbt clean compile run** or the corresponding commands for Gradle. Also, you project should be IntelliJ friendly, i.e., your graders should be able to import your code into IntelliJ and run from there. Use .gitignore to exlude files that should not be pushed into the repo.


## Evaluation criteria
- the maximum grade for this homework is 11%. Points are subtracted from this maximum grade: for example, saying that 2% is lost if some requirement is not completed means that the resulting grade will be 11%-2% => 9%; if the core homework functionality does not work, no bonus points will be given;
- only some basic external command execution code is written and nothing else is done: up to 11% lost;
- having fewer than five external command processing support implemented: up to 10% lost;
- missing comments and explanations from the submitted program: up to 10% lost;
- logging is not used in your programs: up to 3% lost;
- hardcoding the input values in the source code instead of using the suggested configuration libraries: up to 6% lost;
- no instructions in README.md on how to install and run your program: up to 10% lost;
- the program crashes without completing the core functionality: up to 11% lost;
- no design and rule/constraint documentation exists that explains your choices: up to 10% lost;
- the deployment documentation exists but it is insufficient to understand how you assembled and deployed all components of the program: up to 11% lost;
- the minimum grade for this homework cannot be less than zero.

That's it, folks!