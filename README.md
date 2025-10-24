# Added Virtual Proxy Structural Pattern
    - Loads the Proxy Image by default
    - When main thread (mainFrame) is free, it loads the actual image
    - Delayed for 1200 seconds to simulate loading time
    - I plan to add the proxy system again when exiting the screen (i.e During a Game, GameDetail.java, and LoginForm)
        - To do this I'd need to make the threads be attached to an event listener (componentShown and componentHidden) of whether the screen is visible or not
        - However that feature will be heavy on trial and error so I'll version control for now

# Next Agenda
    - Iterator Pattern
    - Database (Majority of the features are locked behind this)
        > Prototype
        > File Generation
        > Visitor Pattern


### Files Modified:
    * NoCapGamesLauncher.java
        1. Skipped the login feature for now by commenting it out
        2. I'm still figuring out the database permissions
            - I'm thinking of just doing it locally rather than on a Virtual Machine
    * resourceLoader.java
    * Library.java
    * Search.java
    * src/main/resources/ImageResources
        1. Added naming conventions to the images
        2. Naming these files should already be handled by the database but to be safe, avoid special characters
        3. The logic is replacing spaces with underscores and turning all characters to lowercase
        4. For convenience, we're sticking to .jpg file extensions
    * src/Resources
