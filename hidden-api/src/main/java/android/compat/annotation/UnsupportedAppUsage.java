package android.compat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 * <p>
 * Used by some AIDL definitions.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Repeatable(UnsupportedAppUsage.Container.class)
public @interface UnsupportedAppUsage {
	long trackingBug() default 0;
	int maxTargetSdk() default Integer.MAX_VALUE;
	String implicitMember() default "";
	String publicAlternatives() default "";

	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.TYPE)
	@interface Container {
		UnsupportedAppUsage[] value();
	}

	String overrideSourcePosition() default "";
	String expectedSignature() default "";
}
