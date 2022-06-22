package uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities;

import java.util.Set;
import java.util.function.Predicate;

public final class DummyEntityTestUtil {


    private DummyEntityTestUtil() {
        //no instantiation
    }


    public static Predicate<Class<?>> getBaseEntitySubclassPredicate() {
        Set<Class<?>> baseEntitySubClasses = Set.of(
                DummyEntityA.class,
                DummyEntityB.class,
                DummyEntityC.class,
                DummyEntityD.class,
                DummyEntityF.class,
                DummyEntityG.class
        );
        return baseEntitySubClasses::contains;
    }


}
