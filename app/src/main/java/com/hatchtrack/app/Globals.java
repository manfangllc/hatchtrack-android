package com.hatchtrack.app;

class Globals {

    private Globals(){}

    static final boolean DEBUG     = true;

    // IDs for CursorLoaders
    // in HatchListFragment
    static final int LOADER_ID_HATCHLIST_HATCHTABLE     = 0x100;
    static final int LOADER_ID_HATCHLIST_SPECIESTABLE   = 0x101;
    //in HatchFragment
    static final int LOADER_ID_HATCH_HATCHTABLE         = 0x110;
    static final int LOADER_ID_HATCH_PEEPTABLE          = 0x111;
    static final int LOADER_ID_HATCH_HATCHPEEPTABLE     = 0x112;
    static final int LOADER_ID_HATCH_SPECIESTABLE       = 0x113;
    // in PeepListFragment
    static final int LOADER_ID_PEEPLIST_PEEPTABLE       = 0x201;
    // in PeepFragment
    static final int LOADER_ID_PEEP_HATCHTABLE          = 0x210;
    static final int LOADER_ID_PEEP_PEEPTABLE           = 0x211;
    // permissions
    static final int PERMISSION_WRITE_CALENDAR = 1;
    static final int PERMISSION_READ_CALENDAR = 2;
    // hatch status
    static final int STATUS_HATCH_UNSTARTED = 1;
    static final int STATUS_HATCH_STARTED = 2;
    static final int STATUS_HATCH_FINISHED = 3;

    // keys for various bundle items
    static final String KEY_HATCH_ID      = "KEY_HATCH_ID";
    static final String KEY_PEEP_NAMES    = "KEY_PEEP_NAMES";
    static final String KEY_PEEP_IDS      = "KEY_PEEP_IDS";
    static final String KEY_PEEP_IN_HATCH = "KEY_PEEP_IN_HATCH";
    static final String KEY_SPECIES_NAMES = "KEY_SPECIES_NAMES";
    static final String KEY_SPECIES_DAYS  = "KEY_SPECIES_DAYS";
    static final String KEY_SPECIES_IDS   = "KEY_SPECIES_IDS";
    static final String KEY_DBID          = "KEY_DBID";
    static final String KEY_SELECT        = "KEY_SELECT";
    static final String KEY_SORT          = "KEY_SORT";
    static final String KEY_URL           = "KEY_URL";
    static final String KEY_SPECIES_PICS_IDS = "KSPI";
    static final String KEY_SPECIES_PICS_STRINGS = "KSPS";
}
