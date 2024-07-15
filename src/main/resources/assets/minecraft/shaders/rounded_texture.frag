#version 120

uniform float round;
uniform sampler2D texture;
uniform vec2 size;
uniform vec4 color; // Nouvelle uniforme pour la couleur

float dstfn(vec2 p, vec2 b) {
    return length(max(abs(p) - b, 0.0)) - round;
}

void main() {
    vec2 tex = gl_TexCoord[0].st;
    vec4 smpl = texture2D(texture, tex);
    vec2 pixel = gl_TexCoord[0].st * size;
    vec2 centre = 0.5 * size;
    float sa = 1.0 - smoothstep(0.0, 1.0, dstfn(centre - pixel, centre - round - 1.0));
    gl_FragColor = vec4(smpl.rgb * color.rgb, smpl.a * color.a * sa); // Applique la couleur
}