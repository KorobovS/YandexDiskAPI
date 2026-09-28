package api.utils;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class StaticParameters {

    public static Stream<Arguments> invalidTokenParameters() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("   "),
                Arguments.of("y0__wgBELbo2XQY25YDIM7e340Zg7INVALID_TOKENBmfOax8h_yXY")
        );
    }

}
