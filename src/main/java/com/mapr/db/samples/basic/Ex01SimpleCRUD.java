/*
 *  Copyright 2009-2015 MapR Technologies
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mapr.db.samples.basic;

import com.mapr.db.Condition;
import com.mapr.db.MapRDB;
import com.mapr.db.DBDocument;
import com.mapr.db.Mutation;
import com.mapr.db.Table;
import com.mapr.db.exceptions.DocumentExistsException;
import com.mapr.db.samples.basic.model.User;
import org.ojai.DocumentStream;

import javax.print.Doc;
import java.io.IOException;
import java.sql.Date;
import java.util.Arrays;
import java.util.Iterator;

import static com.mapr.db.Condition.Op.EQUAL;
import static com.mapr.db.Condition.Op.GREATER_OR_EQUAL;
import static com.mapr.db.Condition.Op.LESS;

/**
 * This class shows the basic operations of Argonaut
 */
public class Ex01SimpleCRUD {

  public static final String TABLE_PATH = "/apps/user_profiles";

  private Table table;


  public Ex01SimpleCRUD() {
  }

  public static void main(String[] args) throws Exception {

    Ex01SimpleCRUD app = new Ex01SimpleCRUD();
    app.run();

  }

  private void run() throws Exception {

    this.deleteTable(TABLE_PATH);
    this.table = this.getTable(TABLE_PATH);
    this.printTableInformation(TABLE_PATH);

    System.out.println("\n\n========== INSERT NEW RECORDS ==========");
    this.createDocuments();


    System.out.println("\n\n========== QUERIES ==========");
    this.queryDocuments();


    System.out.println("\n\n========== UPDATE ==========");
    this.updateDocuments();

    this.table.close();

  }

  /**
   * Get the table, create it if not present
   *
   * @throws IOException
   */
  private Table getTable(String tableName) throws IOException {
    Table table;

    if (!MapRDB.tableExists(tableName)) {
      table = MapRDB.createTable(tableName); // Create the table if not already present
    } else {
      table = MapRDB.getTable(tableName); // get the table
    }
    return table;
  }

  private void deleteTable(String tableName) throws IOException {
    if (MapRDB.tableExists(tableName)) {
      MapRDB.deleteTable(tableName);
    }

  }

  /**
   *
   */
  private void createDocuments() throws IOException {

    // Create a new document (simple format)
    DBDocument document = MapRDB.newDocument()
      .set("_id", "jdoe")
      .set("first_name", "John")
      .set("last_name", "Doe")
      .set("dob", Date.valueOf("1970-06-23"));

    // save document into the table
    table.insertOrReplace(document);


    // create a new document without _id
    document = MapRDB.newDocument()
      .set("first_name", "David")
      .set("last_name", "Simon")
      .set("dob", Date.valueOf("1980-10-13"))
    ;

    table.insert("dsimon", document);


    // create a new document from a simple bean
    // look at the User class to see how you can use JSON Annotation to drive the format of the document
    User user = new User();
    user.setId("alehmann");
    user.setFirstName("Andrew");
    user.setLastName("Lehmann");
    user.setDob(Date.valueOf("1980-10-13"));
    user.addInterest("html");
    user.addInterest("css");
    user.addInterest("js");
    document = MapRDB.newDocument(user);

    // save document into the table
    table.insertOrReplace(document);

    // try to insert the same document ID
    try {
      table.insert("dsimon", document);
    } catch (DocumentExistsException dee) {
      System.out.println("Exception during insert : " + dee.getMessage());
    }


    // Create more complex Record
    document = MapRDB.newDocument()
      .set("_id", "mdupont")
      .set("first_name", "Maxime")
      .set("last_name", "Dupont")
      .set("dob", Date.valueOf("1982-02-03"))
      .set("interests", Arrays.asList("sports", "movies", "electronics"))
      .set("address.line", "1223 Broadway")
      .set("address.city", "San Jose")
      .set("address.zip", 95109)
    ;
    table.insert(document);


    // Another way to create sub document
    // Create the sub document as document and use it to set the value
    DBDocument addressRecord = MapRDB.newDocument()
      .set("line", "100 Main Street")
      .set("city", "San Francisco")
      .set("zip", 94105);

    document = MapRDB.newDocument()
      .set("_id", "rsmith")
      .set("first_name", "Robert")
      .set("last_name", "Smith")
      .set("dob", Date.valueOf("1982-02-03"))
      .set("interests", Arrays.asList("electronics", "music", "sports"))
      .set("address", addressRecord)
    ;
    table.insert(document);


    table.flush(); // flush to the server


  }

  /**
   * Update record see how to : update existing field can add/remove attribute to a document append
   * data into lis of values
   */
  private void updateDocuments() throws IOException {

    {
      System.out.println("\t\tAdd address and status to jdoe");
      System.out.println("before :\t" + table.findById("jdoe"));

      // create a mutation
      Mutation mutation = MapRDB.newMutation()
        .set("active", true)
        .set("address.line", "1015 15th Avenue")
        .set("address.city", "Redwood City")
        .set("address.zip", 94065)
        .build();

      table.update("jdoe", mutation);
      table.flush();

      System.out.println("after :\t\t" + table.findById("jdoe"));
    }


    {
      System.out.println("\n\n\t\tAppend new interests to users");

      // create a mutation
      Mutation mutation = MapRDB.newMutation()
        .append("interests", Arrays.asList("development"))
        .build();

      table.update("jdoe", mutation);
      table.update("mdupont", mutation);
      table.flush();

      System.out.println("after :\t\t" + table.findById("jdoe", "first_name", "last_name", "interests"));
      System.out.println("after :\t\t" + table.findById("mdupont", "first_name", "last_name", "interests"));
    }

    {
      System.out.println("\n\n\t\tRemove attributes (dob)");
      System.out.println("before :\t" + table.findById("jdoe"));

      // create a mutation
      Mutation mutation = MapRDB.newMutation()
        .delete("dob")
        .build();
      table.update("jdoe", mutation);
      table.flush();
      System.out.println("after :\t\t" + table.findById("jdoe"));

    }


  }

  /**
   * Query the record
   */
  private void queryDocuments() throws Exception {

    {
      // get a single document
      DBDocument record = table.findById("mdupont");
      System.out.print("Single record\n\t");
      System.out.println(record);

      //print individual fields
      System.out.println("Id : " + record.getIdString() + " - first name : " + record.getString("first_name"));
    }

    {
      // get a single document
      DBDocument record = table.findById("mdupont", "last_name");
      System.out.print("Single record with projection\n\t");
      System.out.println(record);

      //print individual fields
      System.out.println("Id : " + record.getIdString() + " - first name : " + record.getString("first_name"));

    }

    {
      // get single document and map it to the bean
      User user = table.findById("alehmann").toJavaBean( User.class );
      System.out.println("User Pojo from document : "+ user.toString());
    }

    {
      // all recordss in the table
      System.out.println("\n\nAll records");
      DocumentStream<DBDocument> rs = table.find();
      Iterator<DBDocument> itrs = rs.iterator();
      DBDocument readRecord;
      while (itrs.hasNext()) {
        readRecord = itrs.next();
        System.out.println("\t" + readRecord);
      }
      rs.close();
    }


    {
      // all records in the table with projection
      System.out.println("\n\nAll records with projection");

      try(DocumentStream<DBDocument> documentStream = table.find("first_name", "last_name")) {
        for (DBDocument doc : documentStream ) {
          System.out.println("\t" + doc);
        }
      }
    }


    {
      // all records and use a POJO
      // it is interesting to see how you can ignore unknown attributes with the JSON Annotations
      System.out.println("\n\nAll records with a POJO");

      try(DocumentStream<DBDocument> documentStream = table.find()) {
        for (DBDocument doc : documentStream ) {
          System.out.println("\t" + doc.toJavaBean(User.class));
        }
      }

    }


    {
      // find with condition
      System.out.println("\n\nFind with condition");
      System.out.println("\n\n");

      // Condition equals a string
      Condition condition = MapRDB.newCondition()
        .is("last_name", EQUAL, "Doe")
        .build();
      System.out.println("\n\nCondition: " + condition);
      try(DocumentStream<DBDocument> documentStream = table.find(condition)) {
        for (DBDocument doc : documentStream ) {
          System.out.println("\t" + doc);
        }
      }
    }

    {
      // Condition as date range
      Condition condition = MapRDB.newCondition()
        .and()
        .is("dob", GREATER_OR_EQUAL, Date.valueOf("1980-01-01"))
        .is("dob", LESS, Date.valueOf("1981-01-01"))
        .close()
        .build();
      System.out.println("\n\nCondition: " + condition);
      try(DocumentStream<DBDocument> documentStream = table.find(condition)) {
        for (DBDocument doc : documentStream ) {
          System.out.println("\t" + doc);
        }
      }
    }


    {
      // Condition in sub document
      Condition condition = MapRDB.newCondition()
        .is("address.zip", EQUAL, 95109)
        .build();
      System.out.println("\n\nCondition: " + condition);
      try(DocumentStream<DBDocument> documentStream = table.find(condition)) {
        for (DBDocument doc : documentStream ) {
          System.out.println("\t" + doc);
        }
      }
    }


    {
      // Contains a specific value in an array
      Condition condition = MapRDB.newCondition()
        .is("interests[]", EQUAL, "sports")
        .build();
      System.out.println("\n\nCondition: " + condition);
      try(DocumentStream<DBDocument> documentStream = table.find(condition)) {
        for (DBDocument doc : documentStream ) {
          System.out.println("\t" + doc);
        }
      }
    }

    {
      // Contains a value at a specific index
      Condition condition = MapRDB.newCondition()
        .is("interests[0]", EQUAL, "sports")
        .build();
      System.out.println("\n\nCondition: " + condition);
      try(DocumentStream<DBDocument> documentStream = table.find(condition, "first_name", "last_name", "interests")) {
        for (DBDocument doc : documentStream ) {
          System.out.println("\t" + doc);
        }
      }
    }


  }


  /**
   * Print table information such as Name, Path and Tablets information (sharding)
   *
   * @param tableName
   * @throws IOException
   */
  private void printTableInformation(String tableName) throws IOException {
    Table table = MapRDB.getTable(tableName);
    System.out.println("\n=============== TABLE INFO ===============");
    System.out.println(" Table Name : " + table.getName());
    System.out.println(" Table Path : " + table.getPath());
    System.out.println(" Table Infos : " + Arrays.toString(table.getTabletInfos()));
    System.out.println("==========================================\n");
  }


}
