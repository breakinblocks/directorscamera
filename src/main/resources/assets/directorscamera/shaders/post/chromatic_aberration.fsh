#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform ChromaticConfig {
    vec4 ChromaticSettings;
};

out vec4 fragColor;

void main() {
    float strength = ChromaticSettings.x;
    vec2 offset = (texCoord - vec2(0.5)) * strength;
    vec4 centre = texture(InSampler, texCoord);
    float red = texture(InSampler, clamp(texCoord + offset, vec2(0.0), vec2(1.0))).r;
    float blue = texture(InSampler, clamp(texCoord - offset, vec2(0.0), vec2(1.0))).b;
    fragColor = vec4(red, centre.g, blue, centre.a);
}
