#version 150

uniform sampler2D DiffuseSampler;

uniform float Strength;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec2 offset = (texCoord - vec2(0.5)) * Strength;
    vec4 centre = texture(DiffuseSampler, texCoord);
    float red = texture(DiffuseSampler, clamp(texCoord + offset, vec2(0.0), vec2(1.0))).r;
    float blue = texture(DiffuseSampler, clamp(texCoord - offset, vec2(0.0), vec2(1.0))).b;
    fragColor = vec4(red, centre.g, blue, centre.a);
}
