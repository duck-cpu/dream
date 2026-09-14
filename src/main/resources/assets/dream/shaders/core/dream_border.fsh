#version 150

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    /*
     * Sample the currently animated vanilla Nether portal frame.
     *
     * We care about its moving brightness pattern, not its purple hue.
     */
    vec4 source =
        texture(
            Sampler0,
            texCoord0
        );

    /*
     * Convert the original portal RGB into luminance.
     *
     * This destroys the original purple hue while preserving the
     * animated bright/dark structure of the portal.
     */
    float luminance =
        dot(
            source.rgb,
            vec3(
                0.299,
                0.587,
                0.114
            )
        );

    /*
     * Expand the useful contrast range of the Nether portal texture.
     */
    float energy =
        smoothstep(
            0.04,
            0.72,
            luminance
        );

    /*
     * Dream palette.
     *
     * dark      #092D38
     * mid       #20AEBB
     * bright    #63E5E0
     * highlight #D6FFFF
     */
    vec3 darkColor =
        vec3(
            0.035,
            0.176,
            0.220
        );

    vec3 midColor =
        vec3(
            0.125,
            0.682,
            0.733
        );

    vec3 brightColor =
        vec3(
            0.388,
            0.898,
            0.878
        );

    vec3 highlightColor =
        vec3(
            0.839,
            1.000,
            1.000
        );

    /*
     * Map portal brightness onto our Dream palette.
     */
    vec3 dreamColor =
        mix(
            darkColor,
            midColor,
            smoothstep(
                0.00,
                0.45,
                energy
            )
        );

    dreamColor =
        mix(
            dreamColor,
            brightColor,
            smoothstep(
                0.35,
                0.80,
                energy
            )
        );

    dreamColor =
        mix(
            dreamColor,
            highlightColor,
            smoothstep(
                0.82,
                1.00,
                energy
            )
        );

    /*
     * Alpha remains controlled by DreamBorderRenderer's distance fade.
     *
     * This means changing the shader does not disturb the border
     * visibility behavior we already tuned.
     */
    float alpha =
        source.a *
        vertexColor.a;

    if (alpha <= 0.01) {
        discard;
    }

    fragColor =
        vec4(
            dreamColor,
            alpha
        );
}
