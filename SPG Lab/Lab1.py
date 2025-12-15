import pygame
from pygame.locals import *
from OpenGL.GL import *
from OpenGL.GLUT import *
from OpenGL.GLU import *
import math

def draw_hexagon1():
    glBegin(GL_LINE_LOOP)
    for i in range(6):
        angle_deg = 60 * i + 30
        angle_rad = math.radians(angle_deg)
        x = 100 * math.cos(angle_rad)
        y = 100 * math.sin(angle_rad)
        glVertex2f(x, y)
    glEnd()

def draw_hexagon2():
    glBegin(GL_LINE_LOOP)
    for i in range(6):
        angle_deg = 60 * i + 30
        angle_rad = math.radians(angle_deg)
        x = 250 * math.cos(angle_rad)
        y = 250 * math.sin(angle_rad)
        glVertex2f(x, y)
    glEnd()

def main():
    pygame.init()

    window_width = 800
    window_height = 600
    display = pygame.display.set_mode((window_width, window_height), RESIZABLE | DOUBLEBUF | OPENGL)

    gluOrtho2D(-window_width / 2, window_width / 2, -window_height / 2, window_height / 2)

    running = True
    while running:
        for event in pygame.event.get():
            if event.type == QUIT:
                running = False
            elif event.type == VIDEORESIZE:
                window_width, window_height = event.size
                glViewport(0, 0, window_width, window_height)
                glMatrixMode(GL_PROJECTION)
                glLoadIdentity()
                gluOrtho2D(-window_width / 2, window_width / 2, -window_height / 2, window_height / 2)
                glMatrixMode(GL_MODELVIEW)
                glLoadIdentity()

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        glLoadIdentity()

        glColor3f(2.0, 0.0, 0.0)
        draw_hexagon1()

        glColor3f(1.0, 0.0, 0.0)
        draw_hexagon2()


        pygame.display.flip()

    pygame.quit()

if __name__ == "__main__":
    main()
