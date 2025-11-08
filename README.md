# Preparation for Insane Overhaul
    1.) Fixed Sidebar
        - Dynamic to user value changes now
        - Has a responsive GameDetail panel now instead of copying the recent one as a static panel without its actual values
        - Redirects user to Library instead when logged out panel is GameDetail
    2.) Verification Bugfix
        - Fixed a bug where user login wasn't case-sensitive
        - Fixed a bug where registering a user copies the last user's json if belonging in the same session
    3.) Tracking Changes
        - The Pattern Defense file is deleted. Our group has our own tracker.
        - Deleted .windsurf folder, brodie never reads it.
    4.) Friends Panel Revamp
        - FriendList is now saved in the json file
        - Friends' games are now visible
        - Add/Remove friends now works
        - Waaaaaaayyyyy too many edge cases I didn't even know existed.

# Next Agenda
    - The insane overhaul: JavaFX, Code Cleanup


### Files Modified:
    >> Fixed sidebar <<
    /gui
        mainFrame.java
    /panels
        GameDetail.java
        Library.java
        Store.java

    >> Login Bug Hotfix <<
    /database
        DatabaseHandler.java
    /userManager
        UserRepository.java

    >> Registration Bug Hotfix <<
    /database
        databaseMegaquery.java
    /userManager
        UserGameData.java

    >> Non-Responsive Sidebar fixed <<
    /gui
        sidebarCreator.java
    /panels
        GameDetail.java
        Store.java
    /resourceHandling
        resourceLoader.java

    >> GameDetail - Memento bandaid
    /gui
        mainFrame.java

    >> Friends Panel Revamp
    /gui
        mainFrame.java
    /panels
        Friends.java
    /userManager
        UserGameData.java

    God oh God please make this work. The Friends panel gets freaky randomly.