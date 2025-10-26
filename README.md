# Testing Database Connect (2nd attempt)
    - Install MySQL through (https://dev.mysql.com/downloads/installer)
    - Workbench is the easiest way to prototype (https://dev.mysql.com/downloads/workbench)
        - The website already detects your OS
    - From my experience, root by default is always the top admin. BUT for some reason, it doesn't have all the permissions yet (It can create and grant somehow)
        - Personally, I'm not using root to connect to the database, I created another one named Admin, modify as you will.
    - I created the users table to make "Admin" at the very beginning | "Admin" is accessed via the user table in the database, not the mysql user itself
    - I'm still thinking about what to use the gameURL logic in the database for, but the original idea for that was for the metadata. Which is how the current game knows everything about it, i.e Game Description, Game Image, File Path, etc.

    * Naming Convention
        > Replace spaces with underscores
        > Turn all characters to lowercase
        > Avoid special characters (Though I plan to make that an edge case too, it will be replaced by an underscore)
        > The database being in underscore is not a naming convention, for some reason, MySQL workbench just lowercases it

    - Be very adamant about the connections, I hardcoded most of the Strings.

# Next Agenda
    - Iterator Pattern
    - Prototype
    - File Generation
    - Visitor Pattern


### Files Modified:
    * NoCapGamesLauncher.java
        1. Returned the login feature
        2. "Database Initialized Successfully" is just the creation of the database, the users table, and the Admin user
    * DatabaseHandler.java
    * databaseMegaquery.java
