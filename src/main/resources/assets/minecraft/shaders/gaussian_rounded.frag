#version 120

uniform sampler2D textureIn;
uniform vec2 texelSize, direction;
uniform float blurRadius;
uniform float weights[256];

uniform vec2 size;
uniform float round;
uniform vec4 color;

#define offset texelSize * direction

float alpha(vec2 d, vec2 d1) {
    vec2 v = abs(d) - d1 + round;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - round;
}

void main() {
    vec2 centre = 0.5 * size;
    float roundedAlpha = 1.0 - smoothstep(0.0, 1.5, alpha(centre - (gl_TexCoord[0].st * size), centre - 1.0));

    if (blurRadius == 0.0 || texelSize == vec2(0.0, 0.0) || direction == vec2(0.0, 0.0)) {
        gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, roundedAlpha);
        return;
    }

    vec3 blr = texture2D(textureIn, gl_TexCoord[0].st).rgb * weights[0];

    for (float f = 1.0; f <= blurRadius; f++) {
        blr += texture2D(textureIn, gl_TexCoord[0].st + f * offset).rgb * weights[int(abs(f))];
        blr += texture2D(textureIn, gl_TexCoord[0].st - f * offset).rgb * weights[int(abs(f))];
    }

    gl_FragColor = vec4(blr, roundedAlpha);
}