// server_cs.cs — локальный HTTP-сервер (разработка) на C# (.NET Core)

using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.AspNetCore.StaticFiles;
using System;
using System.IO;
using System.Text.Json;
using System.Threading.Tasks;

namespace DevServer
{
    public class Program
    {
        public static int Port = 8080;
        public static string Root = ".";

        public static void Main(string[] args)
        {
            for (int i = 0; i < args.Length; ++i)
            {
                if (args[i] == "--port" && i+1 < args.Length) Port = int.Parse(args[++i]);
                else if (args[i] == "--root" && i+1 < args.Length) Root = args[++i];
            }

            var host = Host.CreateDefaultBuilder(args)
                .ConfigureWebHostDefaults(webBuilder =>
                {
                    webBuilder.UseUrls($"http://*:{Port}");
                    webBuilder.ConfigureServices(services =>
                    {
                        services.AddCors(options =>
                        {
                            options.AddPolicy("CorsPolicy", builder =>
                                builder.AllowAnyOrigin().AllowAnyMethod().AllowAnyHeader());
                        });
                    });
                    webBuilder.Configure(app =>
                    {
                        app.UseCors("CorsPolicy");
                        app.UseRouting();
                        app.UseEndpoints(endpoints =>
                        {
                            // API
                            endpoints.MapGet("/api/users", async ctx =>
                            {
                                var users = new[] { new { id = 1, name = "Alice" }, new { id = 2, name = "Bob" } };
                                await ctx.Response.WriteAsync(JsonSerializer.Serialize(users));
                            });
                            endpoints.MapPost("/api/users", async ctx =>
                            {
                                ctx.Response.StatusCode = 201;
                                await ctx.Response.WriteAsync("{\"status\":\"created\"}");
                            });
                            endpoints.MapPut("/api/users", async ctx =>
                            {
                                await ctx.Response.WriteAsync("{\"status\":\"updated\"}");
                            });
                            endpoints.MapDelete("/api/users", ctx =>
                            {
                                ctx.Response.StatusCode = 204;
                                return Task.CompletedTask;
                            });
                            // Статика
                            endpoints.MapFallback(async ctx =>
                            {
                                string path = ctx.Request.Path.Value ?? "/";
                                if (path == "/") path = "/index.html";
                                string fullPath = Path.Combine(Root, path.TrimStart('/'));
                                if (File.Exists(fullPath))
                                {
                                    var provider = new FileExtensionContentTypeProvider();
                                    if (!provider.TryGetContentType(fullPath, out var contentType))
                                        contentType = "application/octet-stream";
                                    ctx.Response.ContentType = contentType;
                                    await ctx.Response.SendFileAsync(fullPath);
                                }
                                else
                                {
                                    ctx.Response.StatusCode = 404;
                                    await ctx.Response.WriteAsync("404 Not Found");
                                }
                            });
                        });
                        // Логирование
                        app.Use(async (ctx, next) =>
                        {
                            Console.WriteLine($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] {ctx.Request.Method} {ctx.Request.Path} -> ...");
                            await next();
                            Console.WriteLine($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] {ctx.Request.Method} {ctx.Request.Path} -> {ctx.Response.StatusCode}");
                        });
                    });
                })
                .Build();

            Console.WriteLine($"🌐 DevServer запущен на http://localhost:{Port}");
            Console.WriteLine($"Корневая папка: {Root}");
            Console.WriteLine("Нажмите Ctrl+C для остановки");
            host.Run();
        }
    }
}
