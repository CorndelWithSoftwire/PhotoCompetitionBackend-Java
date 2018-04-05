package org.softwire.training.integration.helpers;

import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static org.hamcrest.CoreMatchers.equalTo;

public class Matchers {
    public static Matcher<Client.ApiResponse> hasStatusCode(int expectedStatusCode) {
        return new FeatureMatcher<Client.ApiResponse, Integer>(
                equalTo(expectedStatusCode),
                "Status Code",
                "hasStatusCode") {
            @Override
            protected Integer featureValueOf(Client.ApiResponse actual) {
                return actual.getStatusCode();
            }
        };
    }

    public static <T> Matcher<Client.ApiResponse<? extends T>> hasStatusCodeWithEntity(int statusCode, Matcher<T> entityMatcher) {
        return new TypeSafeDiagnosingMatcher<Client.ApiResponse<? extends T>>() {
            @Override
            protected boolean matchesSafely(Client.ApiResponse<? extends T> item, Description mismatchDescription) {
                entityMatcher.describeMismatch(
                        item.getEntity(),
                        mismatchDescription
                                .appendText("Status Code was ")
                                .appendValue(item.getStatusCode())
                                .appendText(" and "));
                return item.getStatusCode() == statusCode && entityMatcher.matches(item.getEntity());
            }

            @Override
            public void describeTo(Description description) {
                entityMatcher.describeTo(description
                        .appendText("statusCode should be ")
                        .appendValue(statusCode)
                        .appendText(" and entity should match "));
            }
        };
    }
}
