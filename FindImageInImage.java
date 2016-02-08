
public class FindImageInImage {
    static final int versionMajor = 0;
    static final int versionMinor = 1;

    static final int EXIT_CODE_IMAGE_FOUND = 0
    static final int EXIT_CODE_IMAGE_NOT_FOUND = 1
    static final int EXIT_CODE_INVALID_ARGUMENTS = 2

    public static void main( String[] args ) {
        if ( args.length == 0 ) {
            printUsage();
            System.exit( 0 );
        }

        String filename1 = null;
        String filename2 = null;
        int argIndex = 0;
        while ( argIndex < args.length ) {
            String arg = args[ argIndex ];
            if ( arg.equals( "--help" ) ) {
                printUsage();
                System.exit( 0 );
            } else if ( filename1 == null ) {
                filename1 = arg;
            } else if ( filename2 == null ) {
                filename2 = arg;
            } else {
                println( "ERROR: Invalid argument: " + arg );
                printUsage();
                System.exit( EXIT_CODE_INVALID_ARGUMENTS );
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

