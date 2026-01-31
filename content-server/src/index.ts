#!/usr/bin/env node
import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { CallToolRequestSchema, ErrorCode, ListToolsRequestSchema, McpError } from "@modelcontextprotocol/sdk/types.js";
import { z } from "zod";

// Types for the data structures
interface TocEntry {
  id: string;
  textbookId: string;
  title: string;
  summary?: string;
  pageNumber?: number;
  level: number;
  importance: number;
  keywords: string[];
  orderIndex: number;
  createdAt: number;
}

interface ParagraphResponse {
  requestId: string;
  title: string;
  content: string;
  source: string;
  language: string;
  wordCount: number;
  estimatedReadTime: number;
  tags: string[];
  success: boolean;
  errorMessage?: string;
}

// Mock data for demonstration - in real implementation, this would connect to the actual database
const mockTocEntries: TocEntry[] = [
  {
    id: "toc-1",
    textbookId: "math-geometry",
    title: "Треугольники",
    summary: "Основные понятия и свойства треугольников",
    pageNumber: 45,
    level: 2,
    importance: 4,
    keywords: ["треугольник", "геометрия", "угол", "сторона"],
    orderIndex: 1,
    createdAt: Date.now()
  },
  {
    id: "toc-2",
    textbookId: "math-geometry",
    title: "Виды треугольников",
    summary: "Разносторонние, равнобедренные и равносторонние треугольники",
    pageNumber: 52,
    level: 3,
    importance: 3,
    keywords: ["равнобедренный", "равносторонний", "разносторонний"],
    orderIndex: 2,
    createdAt: Date.now()
  }
];

const mockParagraphs: ParagraphResponse[] = [
  {
    requestId: "para-triangles-001",
    title: "Определение треугольника",
    content: "Треугольник - это геометрическая фигура, образованная тремя отрезками, соединяющими три точки, не лежащие на одной прямой. Эти отрезки называются сторонами треугольника, а точки их пересечения - вершинами.",
    source: "Учебник геометрии",
    language: "ru",
    wordCount: 45,
    estimatedReadTime: 2,
    tags: ["геометрия", "определение", "треугольник"],
    success: true
  }
];

// Create an MCP server
const server = new Server(
  {
    name: "octodiary-content-server",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// Tool: Get table of contents for a textbook
// Handle tool calls
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;
  
  // Input validation for security
  if (!args || typeof args !== 'object') {
    return {
      content: [
        {
          type: "text",
          text: "Invalid arguments: arguments must be provided and be an object"
        }
      ],
      isError: true
    };
  }
  
  // Sanitize string inputs to prevent injection
  const sanitizeString = (input: any): string => {
    if (typeof input !== 'string') return '';
    // Remove potential injection patterns
    return input.replace(/[\x00-\x1F\x7F-\x9F<>'"&]/g, '').trim();
  };
  
  const cacheKey = getCacheKey(name, args);

  // Проверяем кэш для часто используемых запросов
  if (name === "get_table_of_contents" || name === "list_textbooks") {
    const cachedResult = getCachedResult(cacheKey);
    if (cachedResult) {
      return cachedResult;
    }
  }

  let result;
  switch (name) {
case "get_table_of_contents": {
      const { textbookId, level } = args as { textbookId: string; level?: number };
      
      // Validate and sanitize inputs
      if (!textbookId || typeof textbookId !== 'string') {
        result = {
          content: [
            {
              type: "text",
              text: "Invalid argument: textbookId is required and must be a string"
            }
          ],
          isError: true
        };
        break;
      }
      
      const sanitizedTextbookId = sanitizeString(textbookId);
      if (!sanitizedTextbookId) {
        result = {
          content: [
            {
              type: "text",
              text: "Invalid textbookId: contains invalid characters"
            }
          ],
          isError: true
        };
        break;
      }
      
      if (level !== undefined && (typeof level !== 'number' || level < 0 || !Number.isInteger(level))) {
        result = {
          content: [
            {
              type: "text",
              text: "Invalid level: must be a non-negative integer"
            }
          ],
          isError: true
        };
        break;
      }

      try {
        // Filter TOC entries by textbook and optionally by level
        let entries = mockTocEntries.filter(entry => entry.textbookId === sanitizedTextbookId);

        if (level !== undefined) {
          entries = entries.filter(entry => entry.level <= level);
        }

        // Sort by orderIndex
        entries.sort((a, b) => a.orderIndex - b.orderIndex);

        result = {
          content: [
            {
              type: "text",
              text: JSON.stringify({
                textbookId: sanitizedTextbookId,
                entries: entries.map(entry => ({
                  id: entry.id,
                  title: entry.title,
                  summary: entry.summary,
                  pageNumber: entry.pageNumber,
                  level: entry.level,
                  importance: entry.importance,
                  keywords: entry.keywords
                }))
              }, null, 2)
            }
          ]
        };
      } catch (error) {
        result = {
          content: [
            {
              type: "text",
              text: `Error getting table of contents: ${error instanceof Error ? error.message : 'Unknown error'}`
            }
          ],
          isError: true
        };
      }
      break;
    }

    case "request_paragraph": {
      const { topic, language, maxLength } = args as { topic: string; language?: string; maxLength?: number };

      try {
        // In real implementation, this would call the actual ParagraphService
        // For demo, we'll return mock data or simulate a request

        // Simulate finding existing paragraph or creating new request
        const existingParagraph = mockParagraphs.find(p =>
          p.title.toLowerCase().includes(topic.toLowerCase()) ||
          p.content.toLowerCase().includes(topic.toLowerCase())
        );

        if (existingParagraph) {
          return {
            content: [
              {
                type: "text",
                text: JSON.stringify(existingParagraph, null, 2)
              }
            ]
          };
        } else {
          // Simulate creating a new paragraph request
          const newParagraph: ParagraphResponse = {
            requestId: `req-${Date.now()}`,
            title: `Параграф о теме: ${topic}`,
            content: `Это сгенерированный контент по теме "${topic}". В реальной реализации здесь будет подключение к внешнему API для генерации содержимого.`,
            source: "ai_generated",
            language: language || "ru",
            wordCount: 25,
            estimatedReadTime: 1,
            tags: [topic],
            success: true
          };

          return {
            content: [
              {
                type: "text",
                text: JSON.stringify(newParagraph, null, 2)
              }
            ]
          };
        }
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `Error requesting paragraph: ${error instanceof Error ? error.message : 'Unknown error'}`
            }
          ],
          isError: true
        };
      }
    }

case "search_content": {
      const { query, textbookId, limit } = args as { query: string; textbookId?: string; limit?: number };
      
      // Validate and sanitize inputs
      if (!query || typeof query !== 'string') {
        result = {
          content: [
            {
              type: "text",
              text: "Invalid argument: query is required and must be a string"
            }
          ],
          isError: true
        };
        break;
      }
      
      const sanitizedQuery = sanitizeString(query);
      if (!sanitizedQuery) {
        result = {
          content: [
            {
              type: "text",
              text: "Invalid query: contains invalid characters or is empty"
            }
          ],
          isError: true
        };
        break;
      }
      
      const sanitizedTextbookId = textbookId ? sanitizeString(textbookId) : undefined;
      
      if (limit !== undefined && (typeof limit !== 'number' || limit < 1 || limit > 100 || !Number.isInteger(limit))) {
        result = {
          content: [
            {
              type: "text",
              text: "Invalid limit: must be an integer between 1 and 100"
            }
          ],
          isError: true
        };
        break;
      }

      try {
        // Search in TOC entries
        let tocResults = mockTocEntries.filter(entry => {
          if (sanitizedTextbookId && entry.textbookId !== sanitizedTextbookId) return false;

          const searchText = `${entry.title} ${entry.summary || ''} ${entry.keywords.join(' ')}`.toLowerCase();
          return searchText.includes(sanitizedQuery.toLowerCase());
        });

        // Search in paragraphs
        let paragraphResults = mockParagraphs.filter(p => {
          if (sanitizedTextbookId && !p.tags.includes(sanitizedTextbookId)) return false;

          const searchText = `${p.title} ${p.content} ${p.tags.join(' ')}`.toLowerCase();
          return searchText.includes(sanitizedQuery.toLowerCase());
        });

        // Combine and limit results
        const halfLimit = Math.floor((limit || 10) / 2);
        const results = [
          ...tocResults.slice(0, halfLimit).map(entry => ({
            type: "toc_entry",
            id: entry.id,
            title: entry.title,
            summary: entry.summary,
            textbookId: entry.textbookId,
            pageNumber: entry.pageNumber
          })),
          ...paragraphResults.slice(0, halfLimit).map(p => ({
            type: "paragraph",
            id: p.requestId,
            title: p.title,
            content: p.content.substring(0, 200) + "...", // Truncate for search results
            source: p.source,
            tags: p.tags
          }))
].slice(0, limit || 10);

        result = {
          content: [
            {
              type: "text",
              text: JSON.stringify({
                query: sanitizedQuery,
                totalResults: results.length,
                results
              }, null, 2)
            }
          ]
        };
      } catch (error) {
        result = {
          content: [
            {
              type: "text",
              text: `Error searching content: ${error instanceof Error ? error.message : 'Unknown error'}`
            }
          ],
          isError: true
        };
      }
      break;
    }

case "list_textbooks": {
      try {
        // Extract unique textbook IDs from TOC entries
        const textbookIds = [...new Set(mockTocEntries.map(entry => entry.textbookId))];

        const textbooks = textbookIds.map(id => ({
          id,
          name: id.replace('-', ' ').replace(/\b\w/g, l => l.toUpperCase()), // Simple formatting
          tocEntriesCount: mockTocEntries.filter(entry => entry.textbookId === id).length
        }));

        result = {
          content: [
            {
              type: "text",
              text: JSON.stringify({
                textbooks,
                totalCount: textbooks.length
              }, null, 2)
            }
          ]
        };
      } catch (error) {
        result = {
          content: [
            {
              type: "text",
              text: `Error listing textbooks: ${error instanceof Error ? error.message : 'Unknown error'}`
            }
          ],
          isError: true
        };
      }
      break;
    }

default:
      throw new McpError(ErrorCode.MethodNotFound, `Unknown tool: ${name}`);
  }

  // Кэшируем результат для часто используемых запросов
  if (name === "get_table_of_contents" || name === "list_textbooks") {
    setCachedResult(cacheKey, result);
  }

  return result;
});
// Handle listing tools
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: "get_table_of_contents",
        description: "Получить оглавление учебника",
        inputSchema: {
          type: "object",
          properties: {
            textbookId: { type: "string", description: "ID учебника для получения оглавления" },
            level: { type: "number", description: "Максимальный уровень вложенности (по умолчанию все)" }
          },
          required: ["textbookId"]
        }
      },
      {
        name: "request_paragraph",
        description: "Запросить параграф по теме",
        inputSchema: {
          type: "object",
          properties: {
            topic: { type: "string", description: "Тема параграфа" },
            language: { type: "string", description: "Язык текста", default: "ru" },
            maxLength: { type: "number", description: "Максимальная длина в словах" }
          },
          required: ["topic"]
        }
      },
      {
        name: "search_content",
        description: "Поиск контента по ключевым словам",
        inputSchema: {
          type: "object",
          properties: {
            query: { type: "string", description: "Поисковый запрос" },
            textbookId: { type: "string", description: "ID учебника для ограничения поиска" },
            limit: { type: "number", description: "Максимальное количество результатов", default: 10 }
          },
          required: ["query"]
        }
      },
      {
        name: "list_textbooks",
        description: "Получить список доступных учебников",
        inputSchema: {
          type: "object",
          properties: {}
        }
      }
    ]
  };
});

// Оптимизация: кэширование результатов для часто используемых запросов
const cache = new Map<string, { data: any; timestamp: number }>();
const CACHE_TTL = 5 * 60 * 1000; // 5 минут

function getCacheKey(name: string, args: any): string {
  return `${name}:${JSON.stringify(args)}`;
}

function getCachedResult(key: string): any | null {
  const cached = cache.get(key);
  if (cached && (Date.now() - cached.timestamp) < CACHE_TTL) {
    return cached.data;
  }
  cache.delete(key);
  return null;
}

function setCachedResult(key: string, data: any): void {
  cache.set(key, { data, timestamp: Date.now() });
}



        // Search in paragraphs
        let paragraphResults = mockParagraphs.filter(p => {
          if (textbookId && !p.tags.includes(textbookId)) return false;

          const searchText = `${p.title} ${p.content} ${p.tags.join(' ')}`.toLowerCase();
          return searchText.includes(query.toLowerCase());
        });

        // Combine and limit results
        const results = [
          ...tocResults.slice(0, (limit || 10) / 2).map(entry => ({
            type: "toc_entry",
            id: entry.id,
            title: entry.title,
            summary: entry.summary,
            textbookId: entry.textbookId,
            pageNumber: entry.pageNumber
          })),
          ...paragraphResults.slice(0, (limit || 10) / 2).map(p => ({
            type: "paragraph",
            id: p.requestId,
            title: p.title,
            content: p.content.substring(0, 200) + "...", // Truncate for search results
            source: p.source,
            tags: p.tags
          }))
        ].slice(0, limit || 10);

        result = {
          content: [
            {
              type: "text",
              text: JSON.stringify({
                query,
                totalResults: results.length,
                results
              }, null, 2)
            }
          ]
        };
      } catch (error) {
        result = {
          content: [
            {
              type: "text",
              text: `Error searching content: ${error instanceof Error ? error.message : 'Unknown error'}`
            }
          ],
          isError: true
        };
      }
      break;
    }

    case "list_textbooks": {
      try {
        // Extract unique textbook IDs from TOC entries
        const textbookIds = [...new Set(mockTocEntries.map(entry => entry.textbookId))];

        const textbooks = textbookIds.map(id => ({
          id,
          name: id.replace('-', ' ').replace(/\b\w/g, l => l.toUpperCase()), // Simple formatting
          tocEntriesCount: mockTocEntries.filter(entry => entry.textbookId === id).length
        }));

        result = {
          content: [
            {
              type: "text",
              text: JSON.stringify({
                textbooks,
                totalCount: textbooks.length
              }, null, 2)
            }
          ]
        };
      } catch (error) {
        result = {
          content: [
            {
              type: "text",
              text: `Error listing textbooks: ${error instanceof Error ? error.message : 'Unknown error'}`
            }
          ],
          isError: true
        };
      }
      break;
    }

    default:
      throw new McpError(ErrorCode.MethodNotFound, `Unknown tool: ${name}`);
  }

  // Кэшируем результат для часто используемых запросов
  if (name === "get_table_of_contents" || name === "list_textbooks") {
    setCachedResult(cacheKey, result);
  }

  return result;
});

// Start receiving messages on stdin and sending messages on stdout
const transport = new StdioServerTransport();
await server.connect(transport);
console.error('OctoDiary Content MCP server running on stdio with caching enabled');