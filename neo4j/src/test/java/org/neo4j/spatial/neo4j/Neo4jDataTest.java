package org.neo4j.spatial.neo4j;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.spatial.Point;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.values.storable.CoordinateReferenceSystem;
import org.neo4j.values.storable.Values;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class Neo4jDataTest {
    private GraphDatabaseService db;

    @Before
    public void setUp() throws KernelException {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void tearDown() throws Exception {
        db.shutdown();
    }

    @Test
    public void shouldUnderstandNeo4jPointAsPoint() {
        Neo4jPoint neo4jPoint;

        try (Transaction tx = db.beginTx()) {
            Node node = db.createNode(Label.label("PoI"));
            node.setProperty("location", Values.pointValue(CoordinateReferenceSystem.Cartesian, 5.3, 9.1));
            neo4jPoint = new Neo4jPoint(node, "location");
            tx.success();
        }

        assertThat("expected Neo4jPoint to contain correct coordinates", neo4jPoint.getCoordinate(), equalTo(new double[]{5.3, 9.1}));
    }

    @Test
    public void shouldUnderstandPropertyAsPoint() {
        PropertyPoint propertyPoint;

        try (Transaction tx = db.beginTx()) {
            Node node = db.createNode(Label.label("Location"));
            node.setProperty("x", 5.3);
            node.setProperty("y", 9.1);
            propertyPoint = new PropertyPoint(node, "x", "y");
            tx.success();
        }

        assertThat("expected PropertyPoint to contain correct coordinates", propertyPoint.getCoordinate(), equalTo(new double[]{5.3, 9.1}));
    }

    @Test
    public void shouldUnderstandPropertyArrayAsSimplePolygon() {
        Neo4jSimplePolygon neo4jSimplePolygon;

        try (Transaction tx = db.beginTx()) {
            Node node = db.createNode(Label.label("Building"));
            node.setProperty("locations", new Point[]{
                    Values.pointValue(CoordinateReferenceSystem.Cartesian, -10, -10),
                    Values.pointValue(CoordinateReferenceSystem.Cartesian, 10, -10),
                    Values.pointValue(CoordinateReferenceSystem.Cartesian, 10, 10),
                    Values.pointValue(CoordinateReferenceSystem.Cartesian, 0, 20),
                    Values.pointValue(CoordinateReferenceSystem.Cartesian, -10, 10)
            });
            neo4jSimplePolygon = new Neo4jSimplePolygon(node, "locations");
            tx.success();
        }

        assertThat("expected Neo4jSimplePolygon to contain 6 points", neo4jSimplePolygon.getPoints().length, equalTo(6));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 1", neo4jSimplePolygon.getPoints()[0].getCoordinate(), equalTo(new double[]{-10, -10}));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 2", neo4jSimplePolygon.getPoints()[1].getCoordinate(), equalTo(new double[]{10, -10}));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 3", neo4jSimplePolygon.getPoints()[2].getCoordinate(), equalTo(new double[]{10, 10}));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 4", neo4jSimplePolygon.getPoints()[3].getCoordinate(), equalTo(new double[]{0, 20}));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 5", neo4jSimplePolygon.getPoints()[4].getCoordinate(), equalTo(new double[]{-10, 10}));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 6", neo4jSimplePolygon.getPoints()[5].getCoordinate(), equalTo(new double[]{-10, -10}));
    }



    @Test
    public void shouldUnderstandSimpleGraphPolygonAsSimplePolygon() {
        Neo4jSimplePolygon neo4jSimplePolygon;

        try (Transaction tx = db.beginTx()) {
            Node main = db.createNode(Label.label("Main"));

            Node first = db.createNode(Label.label("LocationMarker"));
            first.setProperty("location", Values.pointValue(CoordinateReferenceSystem.Cartesian, -10, -10));
            Node second = db.createNode(Label.label("LocationMarker"));
            second.setProperty("location", Values.pointValue(CoordinateReferenceSystem.Cartesian, 10, -10));
            Node third = db.createNode(Label.label("LocationMarker"));
            third.setProperty("location", Values.pointValue(CoordinateReferenceSystem.Cartesian, 10, 10));
            Node fourth = db.createNode(Label.label("LocationMarker"));
            fourth.setProperty("location", Values.pointValue(CoordinateReferenceSystem.Cartesian, -10, 10));

            main.createRelationshipTo(first, RelationshipType.withName("First"));
            first.createRelationshipTo(second, RelationshipType.withName("Next"));
            second.createRelationshipTo(third, RelationshipType.withName("Next"));
            third.createRelationshipTo(fourth, RelationshipType.withName("Next"));

            neo4jSimplePolygon = new Neo4jSimplePolygon(main, "location", "First", "Next");
            tx.success();
        }

        assertThat("expected Neo4jSimplePolygon to contain 5 points", neo4jSimplePolygon.getPoints().length, equalTo(5));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 1", neo4jSimplePolygon.getPoints()[0].getCoordinate(), equalTo(new double[]{-10, -10}));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 2", neo4jSimplePolygon.getPoints()[1].getCoordinate(), equalTo(new double[]{10, -10}));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 3", neo4jSimplePolygon.getPoints()[2].getCoordinate(), equalTo(new double[]{10, 10}));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 4", neo4jSimplePolygon.getPoints()[3].getCoordinate(), equalTo(new double[]{-10, 10}));
        assertThat("expected Neo4jSimplePolygon to contain correct coordinates on pos 5", neo4jSimplePolygon.getPoints()[4].getCoordinate(), equalTo(new double[]{-10, -10}));
    }
}