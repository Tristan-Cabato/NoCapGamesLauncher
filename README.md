# Memento Pattern completion
    - Reverted the "remember me" checkbox. It's a literal save state of the user's login (Panel, login, user data)

# Friends Panel update
    - Changed the input of the friends to be all the preexisting users. Its still problematic but for the sake of time, let's leave it for later

# Singleton Pattern completion
    - Database connections are now handed over to an instance class of DatabaseHandler
    - Its not intentionally Singleton, but the Facade pattern 'GameManager' is a Singleton

# Iterator Pattern completion
    - All exposed game and user iterations are now handled by Iterator Pattern

# Facade Pattern completion
    - All game management is now handled by the GameManager facade
    - Again it's technically a Singleton Facade but it's not intentionally Singleton

# Next Agenda
    - Sidebar Panel should look more like the first version (Also its broken)
    - Searchbar integration
    - Code cleanup
    - JavaFX integration


### Files Modified:
    >> Memento Pattern complete <<
    /gui
        mainFrame.java
        /panels
            LoginForm.java
    /userManager
        UserMemento.java
    /iterator
        SessionIterator.java

    >> All users -> Friends Panel <<
    /database
        DatabaseHandler.java
    /panels
        Friends.java
    /iterator
        FriendsIterator.java

    >> Class sorting and Database Singleton <<
    /game_manager
        Game.java
        GameMetadata.java
        GameRepository.java
    /gui
        mainFrame.java
        /components
            HeaderCreator.java
            sidebarCreator.java
        /panels
            Friends.java
            GameDetail.java
            Library.java
            LoginForm.java
            Search.java
            Store.java
    /iterator
        FriendsIterator.java
        SessionIterator.java

    >> Pattern Cleanup | 1.0 <<
    /main
        NoCapGamesLauncher.java
    /database
        DatabaseHandler.java
        databaseMegaquery.java
    /gui/panels
        Search.java
    /userManager
        UserGameData.java

    >> Added all necessary patterns <<
    /database
        DatabaseHandler.java
        databaseMegaquery.java
    /game_manager
        GameManager.java
    /gui/components
        HeaderCreator.java
        sidebarCreator.java
    /gui/panels
        GameDetail.java
        Library.java
        Store.java
    /iterator
        GameDescriptionIterator.java
        LibraryGameIterator.java
        StoreGameIterator.java
        UserDataIterator.java
    /resourceHandling
        GameCardData.java
    README.md