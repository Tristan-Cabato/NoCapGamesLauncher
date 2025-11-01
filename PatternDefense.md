
# Creational Patterns:
    - Prototype: To quickly clone a Game metadata object when creating multiple user-owned copies of the same game from the library, instead of querying the database each time.
    - Singleton: For the Library Manager or Installation Manager. This ensures there is a single point of control for installing, uninstalling, and launching games, managing shared resources and state.

# Structural Patterns:
    - Proxy: Use a Virtual Proxy to represent game icons or cover art. The high-resolution image is only loaded from the disk when it actually needs to be displayed in the UI, improving initial load performance.
    - Composite: To organize the game library. A GameCollection (composite) can contain individual Game objects (leafs) or other collections (e.g., "Favorites", "Completed", "Action Games"). This allows for recursive operations like "show total size of collection."

# Behavioral Patterns:
    - Iterator: To provide a standard way to traverse different collections of games (e.g., all games, filtered games, search results) without exposing the underlying data structure (e.g., list, set, tree).
    - Visitor: To define operations that can be performed on the game library without changing the Game or GameCollection classes (e.g., ExportToCSVVisitor, CheckForUpdatesVisitor, CalculateTotalSizeVisitor).

# Current Implementations
    [✓] Prototype:
        - (Game | GameMetadata | GameDetail | UserGameData)
        - Heavy database call for "Game Objects" (GameMetadata - although its reference is a json file) are only queried once and once only: databaseMegaquery. Only the admin can flood the database with games and create JSON files through the database.
        - All Game Object infos are taken from the store.json file which is then stored as a Game Object itself, hidden as a collection of objects in GameRepository and GameIterator.
        - When a user adds a game, the Game Object (from the store.json file) is cloned and placed in the user's ownedGames json, no database call needed.
        - "Is the Game object superficial if it just comes from a json file?" No, it is very much needed for the Iterator pattern to work.
    [-] Singleton
        - We're not done here but in essence I got it in mind. Ironically this will be the last thing I'll be able to do, since the code has to be clean.
        - There are two singleton instances here, the DatabaseManager and a Singleton Facade. Now that's why I said I need a clean code, there are multiple MySQL entry calls and I need to know which classes can be dumbed down to a facade 
    [✓] Proxy 
        - I spent 15 hours here, please notice it.
        - I'm no performance optimizer so the proxy implementation is literally just Thread sleep on instantiation. It will show that the images unload and only load when the panel is opened.
        - I spent 2 days here so I know the classes from the back of my hand: (GameRepository (of course) | resourceLoader | NameFormatting | Library | Search | GameDetail | Store). Enough glazing, its pretty simple: GameRepository loads the data and calls NameFormatting, the Game object is passed to Store and Library. 
        - resourceLoader was a past implementation, wait for the cleanup before I talk about it. It's a fallback game loader for now, it was made before the Iterator pattern. 
    [x] Composite 
        - I just rechecked the initial proposal, thank GOD we didn't mention a customizable game library. We can swap this structural pattern. I don't know what though, oh wait I do 😎😎, I literally mentioned it: Facade Pattern.
        - I may be avoiding this to avoid another hell of class stacking, but also because I literally don't know what to do - we'll have to sort the games in another json I guess? But that defeats the point of Composite, which has a more concrete checking: Objects.
        - I will try though, maybe. Maybe not user defined? We can't exactly sort by genre without either: 
            - A) Manually inserting values in a MySQL table to check for it.
            - B) Having MySQL deal with file directory checking for what folder an executable resides in.
        Which ironically sounds easy as hell, but who knows. We only got 1 day left before the defense.
    [-] Iterator
        - Questionable implementation, and that's because I hardly understand anything. This is currently the 1st push focusing on Iterator. For comparison, Singleton is currently at 4, Prototype at 7, and Proxy at 3.
        - For now the focused Iterator is obviously, Game Iterator. The ones called by Store and Library.
        - There are still multiple Collection exposure: resourceLoader shows a Map, NameFormatting shows an ArrayList, Store iterates over a field, and God oh God every JFrame has a component iteration. I'll work on it I think.
    [x] Visitor
        - I... don't know what this is? We create a User json when they login, is that Visitor? We add a game to the user's ownedGames json, is that Visitor? (It's in Prototype by the way) We do have a system logging to show how many games are loaded, is that Visitor?
        - Anyway, Mediator and Memento are valid implementation swaps for this Behavioral Pattern, but Visitor sounds okay. 