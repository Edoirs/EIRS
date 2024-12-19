package ng.gov.eirs.mas.erasmpoa.data;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by himanshusoni on 18/08/17.
 */

@Database(name = DB.NAME, version = DB.VERSION)
public class DB {
    public static final String NAME = "eras"; // we will add the .db extension

    public static final int VERSION = 4;

    // version 4 added Haulage table

    // version 3 added Beat table

    // version 2 added ScratchCard table

    //    @Migration(version = 3, database = DB.class)
    //    public static class Migration3PriceSheet extends AlterTableMigration<PriceSheet> {
    //        public Migration3PriceSheet(Class<PriceSheet> table) {
    //            super(table);
    //        }
    //
    //        @Override
    //        public void onPreMigrate() {
    //            super.onPreMigrate();
    //            addColumn(SQLiteType.INTEGER, PriceSheet_Table.active.toString());
    //            addColumn(SQLiteType.INTEGER, PriceSheet_Table.deleted.toString());
    //        }
    //    }
}
