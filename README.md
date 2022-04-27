## Antaeus
The challenge took me 3-4 hours spread over 3 days.
### TLDR - Summary
- Used a cron job to schedule pending invoices processing to run every hour of the first day of each month.
- Successful processing on an invoice updates its status to `PAID` 
- For currency mismatch we set invoice status to `INVALID` and create a new one with converted amount & currency.
- For insufficient balance we suspend the customer.
- For network errors we retry the processing after a delay.
- For customer not found we set the invoice status to `FAILED`.
## Solution
The way I thought about this is the following, there are 4 parts to invoice processing, error handling, scheduling and retrying, but to start off
lets maybe set the scope and clear out the assumptions, 
### Scope & Assumptions
#### Invoice Creation 
To be able to process invoices there should be something that actually creates them
. I believe that is beyond the scope of this challenge, so let's assume invoices will be created externally.
#### Insufficient customer balance
When the account balance is not enough to pay the subscription amount, there are multiple ways we can handle that.
Keeping the invoice pending and retrying later on for example, but I decided to go with a simpler approach.
Simply suspending the account, and let's assume there is a way for customer to manually pay to reactivate their subscription.
#### Currency conversion
To be able to handle `CurrencyMismatchException` we need to be able to convert amounts between different currencies, 
so let's assume we have another external service that can do that.

### Invoice Processing
Processing invoices is very straight forward, charge the customer then update the status of the invoice to `PAID`
### Error handling
There are two types of error, unrecoverable errors like `CustomerNotFoundException` where the only thing we can do
is update the status of the invoice to `FAILED`, and errors that we can handle like `NetworkException` where we can 
retry after a certain cool down
- `InsufficientBalanceException` -> error log, set invoice status to `FAILED` and suspend customer
- `NetworkException` -> error log and retry after a 5 minutes delay
- `CustomerNotFoundException` -> error log and set invoice status to `FAILED`
- `CurrencyMismatchException` -> warning log,set invoice status to `INVALID` and create a new invoice with the converted value/currency

### Scheduling
This was the part I thought about the most given how many approaches I could've gone with. at the end I decided to not reinvent the wheel
and go with a cron job, since the challenge explicitly mentions that the invoice processing should be done on the first of each month.
For that I used a library `Krontab`
### Retrying
For retrying, I just went with a loop with a condition on attempts over maximum attempts, with putting the thread
asleep for a delay, simple yet does the job well.
***
Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!
