
public class FindImageInImage {
    static final int versionMajor = 0;
    static final int versionMinor = 1;

    public static void main( String[] args ) {
        if ( args.length == 0 ) {
            printUsage();
            System.exit( 0 );
        }

        int argIndex = 0;
        while ( argIndex < args.length ) {
            String arg = args[ argIndex ];
            if ( arg.equals( "--help" ) ) {
                printUsage();
                System.exit( 0 );
            }
            argIndex++;
        }
    }

    static void exitWithMessage( String message, boolean printUsage, int exitCode ) {
        println( message );
        if ( printUsage ) {
            printUsage();
        }
        System.exit( exitCode );
    }

    static void printUsage() {
        println( "USAGE:" );
        println();
        println( " FindImageInImage [--help] [image_to_find [image_to_search_in]]" );
        println();
        println( " Search for image in another image or the screen." );
        println();
        println("PARAMETERS:");
        println();
        println(" image_to_find");
        println("   File name of image to search for");
        println();
        println(" image_to_search_in");
        println("   File name of image to search in.");
        println("   Searches on screen if not specified.");
        println();
        println(" --help");
        println("   This information");
        println();
        println( "EXIT CODES:" );
        println();
        println( " 0 : Sub-image has been found" );
        println( " 1 : Sub-image has not been found" );
        println( " 2 : Error in parameters" );
        println();
    }

    static void printVersion() {
        println( "FindImageInImage v" + versionMajor + "." + versionMinor );
    }

    static void println(String message) {
        System.out.println( message );
    }

    static void println() {
        System.out.println();
    }
}

