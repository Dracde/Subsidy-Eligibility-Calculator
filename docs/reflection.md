# Personal Reflection

## 1. OOP concepts applied

The first OOP concept applied is encapsulation. Encapsulation means hiding a class's
internal details and only letting other code access them through controlled methods, instead
of letting anyone change the data directly. I used this in Repository[T], where the table
name is a private val — only the repository itself can use it, nothing outside can touch it.

The second is inheritance. Inheritance means one class can reuse the code of another
class instead of writing everything again. HouseholdRepository, SubsidyRepository, and
ApplicationRepository all extend the same Repository[T] class, so the "find all" and
"delete" code is written once and shared by all three.

The third is polymorphism. Polymorphism means different classes can respond to the same
method call in their own way. ApplicationStatus is a sealed trait with three versions —
Pending, Approved, Rejected — and each one gives back its own color and label when asked,
without needing an if/else chain anywhere else in the code.

The fourth is abstraction. Abstraction means showing only what's necessary and hiding the
complicated parts. EligibilityService just exposes simple methods like
eligibleSubsidiesFor(household), and the controllers don't need to know how that decision
is actually made underneath.

## 2. Problems faced

Most of the problems I ran into are ones a lot of people run into when learning a new
framework, not anything special to my project. The first was just getting the project to
compile at all when I started, such as missing dependencies and small syntax mistakes in build.sbt,
which is a very common first problem with sbt projects. The second was the app compiling fine
but crashing when I actually ran it, because of a missing database driver dependency which caused
"it builds but it doesn't run" problem. The third was a table in my UI just showing up
empty with no error message at all, which was confusing until I found out it was a reflection
issue between Scala and Java, not a bug in my own logic.

## 3. Strengths / weaknesses

A strength of my project is that the eligibility-checking logic (`EligibilityService`) is
separated from the database code and the UI code, so I could test it on its own without
needing to run the whole app. I think this makes the project easier to understand, since each
part only does one job. This perfectly demonstrates the Single Responsibility Principle and Separation of Concerns.  A weakness is that some of my code is repetitive because I had to write a
very similar column-binding setup for every table in the app because Scala case classes don't
work directly with JavaFX's usual shortcuts, so there's more repeated code than I'd like. If I
had more time, I'd try to write one shared helper for this instead of repeating it in every
controller.