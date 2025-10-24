#!/usr/bin/env node
import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { CallToolRequestSchema, ErrorCode, ListToolsRequestSchema, McpError } from "@modelcontextprotocol/sdk/types.js";
// Mock data for demonstration - in real implementation, this would connect to the actual database
const mockTocEntries = [
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
const mockParagraphs = [
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
const server = new Server({
    name: "octodiary-content-server",
    version: "1.0.0",
}, {
    capabilities: {
        tools: {},
    },
});
// Tool: Get table of contents for a textbook
// Handle tool calls
server.setRequestHandler(CallToolRequestSchema, async (request) => {
    const { name, arguments: args } = request.params;
    switch (name) {
        case "get_table_of_contents": {
            const { textbookId, level } = args;
            try {
                // Filter TOC entries by textbook and optionally by level
                let entries = mockTocEntries.filter(entry => entry.textbookId === textbookId);
                if (level !== undefined) {
                    entries = entries.filter(entry => entry.level <= level);
                }
                // Sort by orderIndex
                entries.sort((a, b) => a.orderIndex - b.orderIndex);
                return {
                    content: [
                        {
                            type: "text",
                            text: JSON.stringify({
                                textbookId,
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
            }
            catch (error) {
                return {
                    content: [
                        {
                            type: "text",
                            text: `Error getting table of contents: ${error instanceof Error ? error.message : 'Unknown error'}`
                        }
                    ],
                    isError: true
                };
            }
        }
        case "request_paragraph": {
            const { topic, language, maxLength } = args;
            try {
                // In real implementation, this would call the actual ParagraphService
                // For demo, we'll return mock data or simulate a request
                // Simulate finding existing paragraph or creating new request
                const existingParagraph = mockParagraphs.find(p => p.title.toLowerCase().includes(topic.toLowerCase()) ||
                    p.content.toLowerCase().includes(topic.toLowerCase()));
                if (existingParagraph) {
                    return {
                        content: [
                            {
                                type: "text",
                                text: JSON.stringify(existingParagraph, null, 2)
                            }
                        ]
                    };
                }
                else {
                    // Simulate creating a new paragraph request
                    const newParagraph = {
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
            }
            catch (error) {
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
            const { query, textbookId, limit } = args;
            try {
                // Search in TOC entries
                let tocResults = mockTocEntries.filter(entry => {
                    if (textbookId && entry.textbookId !== textbookId)
                        return false;
                    const searchText = `${entry.title} ${entry.summary || ''} ${entry.keywords.join(' ')}`.toLowerCase();
                    return searchText.includes(query.toLowerCase());
                });
                // Search in paragraphs
                let paragraphResults = mockParagraphs.filter(p => {
                    if (textbookId && !p.tags.includes(textbookId))
                        return false;
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
                return {
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
            }
            catch (error) {
                return {
                    content: [
                        {
                            type: "text",
                            text: `Error searching content: ${error instanceof Error ? error.message : 'Unknown error'}`
                        }
                    ],
                    isError: true
                };
            }
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
                return {
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
            }
            catch (error) {
                return {
                    content: [
                        {
                            type: "text",
                            text: `Error listing textbooks: ${error instanceof Error ? error.message : 'Unknown error'}`
                        }
                    ],
                    isError: true
                };
            }
        }
        default:
            throw new McpError(ErrorCode.MethodNotFound, `Unknown tool: ${name}`);
    }
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
const cache = new Map();
const CACHE_TTL = 5 * 60 * 1000; // 5 минут
function getCacheKey(name, args) {
    return `${name}:${JSON.stringify(args)}`;
}
function getCachedResult(key) {
    const cached = cache.get(key);
    if (cached && (Date.now() - cached.timestamp) < CACHE_TTL) {
        return cached.data;
    }
    cache.delete(key);
    return null;
}
function setCachedResult(key, data) {
    cache.set(key, { data, timestamp: Date.now() });
}
// Handle tool calls with caching
server.setRequestHandler(CallToolRequestSchema, async (request) => {
    const { name, arguments: args } = request.params;
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
        // ... остальной код без изменений
        case "get_table_of_contents": {
            const { textbookId, level } = args;
            try {
                // Filter TOC entries by textbook and optionally by level
                let entries = mockTocEntries.filter(entry => entry.textbookId === textbookId);
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
                                textbookId,
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
            }
            catch (error) {
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
            const { topic, language, maxLength } = args;
            try {
                // In real implementation, this would call the actual ParagraphService
                // For demo, we'll return mock data or simulate a request
                // Simulate finding existing paragraph or creating new request
                const existingParagraph = mockParagraphs.find(p => p.title.toLowerCase().includes(topic.toLowerCase()) ||
                    p.content.toLowerCase().includes(topic.toLowerCase()));
                if (existingParagraph) {
                    result = {
                        content: [
                            {
                                type: "text",
                                text: JSON.stringify(existingParagraph, null, 2)
                            }
                        ]
                    };
                }
                else {
                    // Simulate creating a new paragraph request
                    const newParagraph = {
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
                    result = {
                        content: [
                            {
                                type: "text",
                                text: JSON.stringify(newParagraph, null, 2)
                            }
                        ]
                    };
                }
            }
            catch (error) {
                result = {
                    content: [
                        {
                            type: "text",
                            text: `Error requesting paragraph: ${error instanceof Error ? error.message : 'Unknown error'}`
                        }
                    ],
                    isError: true
                };
            }
            break;
        }
        case "search_content": {
            const { query, textbookId, limit } = args;
            try {
                // Search in TOC entries
                let tocResults = mockTocEntries.filter(entry => {
                    if (textbookId && entry.textbookId !== textbookId)
                        return false;
                    const searchText = `${entry.title} ${entry.summary || ''} ${entry.keywords.join(' ')}`.toLowerCase();
                    return searchText.includes(query.toLowerCase());
                });
                // Search in paragraphs
                let paragraphResults = mockParagraphs.filter(p => {
                    if (textbookId && !p.tags.includes(textbookId))
                        return false;
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
            }
            catch (error) {
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
            }
            catch (error) {
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
//# sourceMappingURL=index.js.map