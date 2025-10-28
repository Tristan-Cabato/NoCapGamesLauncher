# Admin Database Megaquery
    - Handles database creation, Table creation, and metadata creation
    - Ideally, this and logging in are the only ones that should access the database, everything else will be through Prototype Pattern
    - User ID generation is a bit janky right now. Unlike gamemetadata which is half-assed by truncating the table each generation, the User must exist at all times.
    - The User table is a whole different story. It will be used for the Friends panel.

# App Scanning
    - Slowly transferring game scanning logic to json files. This way, its easy to map values
    - Again, my bad, I don't know how to make it dynamic for MacOS

# Game Store
    - First to be transferred to json files
    - This panel has all the games in the database, so it makes sense.
        - Plan here is, the "Play" button is dynamic whether the current user owns the game or not.
        - Adding the game puts it in the Library Panel, and by extension - the sidebar.
    - Of course, this would mean that the source of the Library Panel is no longer library_games.txt, but the owned games key in the user json.
        - PlayerOwnedGames[1, 5, 9] -> These Hash keys point to store_games.json, that's how its fetched
        - Prototype in the instruction states "clone a Game metadata object when creating multiple user-owned copies of the same game," this is one. We aren't cloning an object per se, hell, we should avoid doing that - but we are "duplicating" values of a game without querying the database (Pointing to the same file).

# Next Agenda
    UI:
        - Sidebar Panel should look more like the first version
        - Remove the remember account checkbox, it will never be used
        - Searchbar integration
        - GameDetail.java is due for an overhaul 
    
    Metadata Interaction:
        - As stated above, Library.java should reflect on the user metadata
        - This would mean that upon login, Library and Sidebar should be relative to the current user metadata
        - The User Metadata files are already there, I just don't know what the hell I'm doing

    Put ts in the end of the stack:
        - Iterator Pattern
        - Visitor Pattern
        - JavaFX integration


### Files Modified:
    >> Bout to touch the users (I am so sorry for how I name commits) <<
    /database
        ↳ DatabaseHandler.java
        ↳ databaseMegaquery.java
    /components
        ↳ HeaderCreator.java
            - Changed "ChangeAccount" and "Sign In/Login" to a singular "Logout"
    /gui
        ↳ Library.java
        ↳ Store.java
        ↳ LoginForm.java
        ↳ Profile.java
        ↳ Search.java
    /resourceHandling
        ↳ resourceLoader.java
    /userManager
        ↳ GameMetadata.java
        ↳ UserGameData.java

    >> Game Store diddit <<
    /database
        ↳ DatabaseHandler.java
        ↳ databaseMegaquery.java
    /gui
        ↳ GameDetail.java
        ↳ Search.java
        ↳ Store.java
    /resourceHandling
        ↳ resourceLoader.java
        ↳ NameFormatting.java
    /userManager
        ↳ GameMetadata.java

    >> Game Launching <<
    /gui
        ↳ GameDetail.java

    >> Megaquery, json Files, and Executables <<
    README.md