# Separation of concerns
    - Split Executable scanning (to database) and JSON generation (File Generation)
    - This is to show that the database is accessed, not just a one time process

# Library
    - Made the sidebar and the library panel reflect on the user metadata. Text files are no longer required.

# Next Agenda
    UI:
        - Sidebar Panel should look more like the first version
        - Remove the remember account checkbox, it will never be used
        - Searchbar integration
        - GameDetail.java is due for an overhaul 
    
    Metadata Interaction:
        - Game Description should reflect on the metadata
        - Play button should be dynamic
        - The User Metadata files are already there, I just don't know what the hell I'm doing

    Put ts in the end of the stack:
        - Iterator Pattern
        - Visitor Pattern
        - JavaFX integration


### Files Modified:
    >> Database | FileGen separate of concern <<
    /database
        ↳ databaseMegaquery.java
    
    >> Created user jsons <<
    /pom.xml
        - Added gson dependency. Imma be real I don't even know how it works but it keeps getting suggested
    /database
        ↳ DatabaseHandler.java
    /panels
        ↳ GameDetail.java
        ↳ Library.java
        ↳ LoginForm.java
    /userManager
        ↳ UserGameData.java
        ↳ UserRepository.java
    /components
        ↳ sidebarCreator.java
    
    >> Added User Library logic <<
    /database
        ↳ DatabaseHandler.java
    /panels
        ↳ Library.java
        ↳ LoginForm.java
    /components
        ↳ sidebarCreator.java
    /resourceHandling
        ↳ resourceLoader.java
    /src/main/resources/Users
        ↳ Test.json
        ↳ safi.json
    
    >> User Library Integration <<
        ↳ Readme.md
