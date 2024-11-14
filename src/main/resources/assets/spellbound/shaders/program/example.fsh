#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 original = texture(DiffuseSampler, texCoord);
    vec3 tint = vec3(sin(Time) / 2.0 + 0.5F, cos(Time) / 2.0 + 0.5F, 0.0F);
    vec4 result = original * vec4(tint, 1.0);
    fragColor = result;
}